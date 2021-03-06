package de.zalando.platform.awsutilizationmonitor.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.regions.Regions;

import de.zalando.platform.awsutilizationmonitor.api.view.StatsTable;
import de.zalando.platform.awsutilizationmonitor.collector.AwsStatsCollector;
import de.zalando.platform.awsutilizationmonitor.config.Config;
import de.zalando.platform.awsutilizationmonitor.stats.AwsResource;
import de.zalando.platform.awsutilizationmonitor.stats.AwsResourceType;
import de.zalando.platform.awsutilizationmonitor.stats.AwsStats;
import de.zalando.platform.awsutilizationmonitor.stats.AwsStatsSummary;
import de.zalando.platform.awsutilizationmonitor.stats.AwsTag;

@RestController
final class AwsUtilizationMonitorController {
	public static final Logger LOG = LoggerFactory.getLogger(AwsUtilizationMonitorController.class);

	private AwsStatsCollector collector;

	@Autowired
	public AwsUtilizationMonitorController(AwsStatsCollector collector) {
		this.collector = collector;
	}

	@RequestMapping("/accounts/")
	@ResponseBody
	String[] accounts() {
		LOG.info("called /accounts/");

		return collector.getStats().getAccounts();
	}

	@RequestMapping(value = "/accounts/", method = RequestMethod.PUT)
	@ResponseBody
	void accounts(@RequestBody AwsAccount[] accounts) {
		LOG.info("called PUT /accounts/");

		collector.setAccounts(accounts);

		LOG.info("added " + accounts.length + " accounts");
	}

	@RequestMapping("/accounts/{accountName}/")
	@ResponseBody
	AwsResource[] accounts(@PathVariable String accountName) {
		accountName = decodeParam(accountName);
		LOG.info("called /accounts/" + accountName + "/");

		AwsResource[] results = collector.getStats().getResourcesByAccount(accountName);

		if ((results == null) || (results.length == 0)) {
			LOG.info("No resource found for account \"" + accountName + "\"!");
		}

		return results;
	}

	@RequestMapping("/amis/")
	@ResponseBody
	String[] amis() {
		LOG.info("called /amis/");

		return collector.getStats().getUsedAMIs();
	}

	@RequestMapping("/amis/{ami}/")
	@ResponseBody
	AwsResource[] amis(@PathVariable String ami) {
		ami = decodeParam(ami);
		LOG.info("called /amis/" + ami + "/");

		AwsResource[] res = collector.getStats().searchResources(AwsTag.AMI, ami);

		if ((res == null) || (res.length == 0)) {
			LOG.info("No instance found with AMI \"" + ami + "\"!");
		}

		return res;
	}

	@RequestMapping("/apps/")
	@ResponseBody
	String[] apps() {
		LOG.info("called /apps/");

		return collector.getStats().getApps();
	}

	@RequestMapping("/apps/{appName}/")
	@ResponseBody
	AwsResource[] apps(@PathVariable String appName) {
		appName = decodeParam(appName);
		LOG.info("called /apps/" + appName + "/");

		AwsResource[] res = collector.getStats().getAppInstances(appName);

		if ((res == null) || (res.length == 0)) {
			LOG.info("No app found with name \"" + appName + "\"!");
		}

		return res;
	}

	// @RequestMapping("/credentials/")
	// @ResponseBody
	// AwsAccount[] credentials() {
	// LOG.info("called /credentials/");
	//
	// List<AwsAccount> accounts = new ArrayList<AwsAccount>();
	// accounts.add(new AwsAccount(
	// "123456789012 (account-name1)",
	// "ASIAI345ZKOMUQ76JZ2A",
	// "LVxrdHkDGZKU566VuTQxFvc1gJOSHdSN095H2xim",
	// "AQoDYXd45645645SV4ZYUGeo5uF5FQYsCcgOUBTIGe8blGuCGfLfmk51k3Ij3aSu6dIBQ2poLEm1SkcRD7S7MPdqrZSEnD1+4N8XRuZvyT9Z29DO5ZcAw0cVoBJWyGx+U68cywsfZLN3SqRkr+NvT5Xlg+ashwHPs/q4r4QoqjOOJ9M+y7leY4RmGNDfbTbRDgUY2tSttS9/fGS0KvprUPC4gOi6WwehrWcbw8NBfFUTtfP+G4YPnZB/ZJ0Jmc1IkbyIxLkzyZUysvhhAnmJffrrMCIPmF+GUyaTVsLLJ3Gwhy5tNNNEd7beH76t1G0euHKRkX6/ewqClITzE7wQtkpKDZYgLbHfCR4gfjfsH3+6KyP1XWZ46gbSrzTevG453ggTGWuuFAExMRNE2y1RJD46+twriMZRKuwi8mjGc554rz4Z5M4MKEGErv0qKf7jnrGVjIAtDYX30oyrNPb8eAMGDCHs/Fe5bfR4bSDr6PSmBQ=="));
	// accounts.add(new AwsAccount("100000000000 (account-name2)",
	// "bla", "blub", "SESSION"));
	// accounts.add(new AwsAccount(
	// "999999999999 (account-name3)",
	// "ASIAI345ZKOMUQ76JZ2A",
	// "LVxrdHkDGZKU566VuTQxFvc1gJOSHdSN095H2xim",
	// "AQoDYXdzELD//////////wEa8AK2ZsPsLEh1pZKAVKk7oMWSV4ZYUGeo5uF5FQYsCcgOUBT235235blGuCGfLfmk51k3Ij3aSu6dIBQ2poLEm1SkcRD7S7MPdqrZSEnD1+4N8XRuZvyT9Z29DO5ZcAw0cVoBJWyGx+U68cywsfZLN3SqRkr+NvT5Xlg+ashwHPs/q4r4QoqjOOJ9M+y7leY4RmGNDfbTbRDgUY2tSttS9/fGS0KvprUPC4gOi6WwehrWcbw8NBfFUTtfP+G4YPnZB/ZJ0Jmc1IkbyIxLkzyZUysvhhAnmJffrrMCIPmF+GUyaTVsLLJ3Gwhy5tNNNEd7beH76t1G0euHKRkX6/ewqClITzE7wQtkpKDZYgLbHfCR4gfjfsH3+6KyP1XWZ46gbSrzTevG453ggTGWuuFAExMRNE2y1RJD46+twriMZRKuwi8mjGc554rz4Z5M4MKEGErv0qKf7jnrGVjIAtDYX30oyrNPb8eAMGDCHs/Fe5bfR4bSDr6PSmBQ=="));
	//
	// return accounts.toArray(new AwsAccount[accounts.size()]);
	// }

	@RequestMapping("/clear/")
	@ResponseBody
	String clear() {
		LOG.info("called /clear/");

		collector.clearCache();

		return "Cache empty";
	}

	@RequestMapping("/config/")
	@ResponseBody
	Config config() {
		LOG.info("called /config/");

		return collector.getConfig();
	}

	@RequestMapping(value = "/config/", method = RequestMethod.PUT)
	@ResponseBody
	void config(@RequestBody Config config) {
		LOG.info("called PUT /config/");

		collector.setConfig(config);

		LOG.info("overwrote config " + config.toString());
	}

	String decodeParam(String param) {
		try {
			return URLDecoder.decode(param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error("Cannot decode \"" + param + "\": " + e.getMessage());
		}

		return param;
	}

	String encodeParam(String param) {
		try {
			// UrlEscapers.urlPathSegmentEscaper().escape(s1)
			return URLEncoder.encode(param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error("Cannot encode \"" + param + "\": " + e.getMessage());
		}

		return param;
	}

	@RequestMapping("/force/")
	@ResponseBody
	AwsStats force() {
		LOG.info("called /force/");

		return collector.forceAddStats();
	}

	@RequestMapping("/health")
	@ResponseBody
	String health() {
		return "OK";
	}

	@RequestMapping("/")
	@ResponseBody
	String home() {
		LOG.info("called /");

		return "<html><header><style>p, li, ul, a { font-family:'Courier New', Arial; }</style></header><body><h1>AWS Utilization Statistics</h1><p><ul>"
				+ "<li><a href=/accounts/>/accounts/</a> List accounts</li>"
				+ "<li><a href=/accounts/123456789012/>/accounts/{account_name}/</a> Show resources used by account with name \"123456789012\"</li>"
				+ "<li><a href=/apps/>/apps/</a> List EC2 based apps</li>"
				+ "<li><a href=/apps/NAT/>/apps/{app_name}/</a> Show EC2 based apps with name \"NAT\"</li>"
				+ "<li><a href=/clear/>/clear/</a> Clear data cache</li>"
				+ "<li><a href=/health/>/health/</a> Show health</li>"
				+ "<li><a href=/instancetypes/>/instancetypes/</a> List used EC2 instance types</li>"
				+ "<li><a href=/instancetypes/"
				+ encodeParam("t2.micro")
				+ "/>/instancetypes/{instance_type}/</a> Show EC2 based apps with instance type \"t2.micro\"</li>"
				+ "<li><a href=/keys/>/keys/</a> List keys</li>"
				+ "<li><a href=/keys/PublicDnsName/>/keys/{key_name}/</a> Show resources that contain a value with the key \"PublicDnsName\"</li>"
				+ "<li><a href=/regions/>/regions/</a> List regions</li>"
				+ "<li><a href=/regions/EU_WEST_1/>/regions/{region_name}/</a> Show resources used by region with name \"EU_WEST_1\"</li>"
				+ "<li><a href=/resources/>/resources/</a> List resources</li>"
				+ "<li><a href=/resources/NAT/>/resources/{resource_name}/</a> Show resources with name \"NAT\"</li>"
				+ "<li><a href=/search/banana/>/search/{search_pattern}/</a> Show app with name \"banana\"</li>"
				+ "<li><a href=/statistics/>/statistics/</a> Show statistics about resource usage</li>"
				+ "<li><a href=/summary/>/summary/</a> Show summary KPIs only about resource usage</li>"
				+ "<li><a href=/teams/>/teams/</a> List team names if \"team\" tag was specified</li>"
				+ "<li><a href=/teams/Platform/>/teams/{team_name}/</a> Show resources of team \"Platform\"</li>"
				+ "<li><a href=/test/>/test/</a> Generate test data</li>"
				+ "<li><a href=/test/30/>/test/{maxItems}</a> Generate test data with 30 items</li>"
				+ "<li><a href=/values/Team/Platform/>/values/{key_name}/{value_pattern}/</a> Show resources that contain a value with the key \"Team\" and the pattern \"Platform\"</li>"
				+ "</ul></p></body></html>";
	}

	@RequestMapping("/instancetypes/")
	@ResponseBody
	String[] instancetypes() {
		LOG.info("called /instancetypes/");

		return collector.getStats().getUsedEC2InstanceTypes();
	}

	@RequestMapping("/instancetypes/{instanceType}/")
	@ResponseBody
	AwsResource[] instancetypes(@PathVariable String instanceType) {
		LOG.info("called /instancetypes/" + instanceType + "/");

		AwsResource[] res = collector.getStats().getResourcesByEC2InstanceType(instanceType);

		if ((res == null) || (res.length == 0)) {
			LOG.info("No instance found with instance type \"" + instanceType + "\"!");
		}

		return res;
	}

	@RequestMapping("/keys/")
	@ResponseBody
	String[] keys() {
		LOG.info("called /keys/");

		return collector.getStats().getKeys();
	}

	@RequestMapping("/keys/{keyName}/")
	@ResponseBody
	Object[] keys(@PathVariable String keyName) {
		LOG.info("called /keys/" + keyName + "/");

		collector.getStats().getValues(keyName);
		Object[] results = collector.getStats().getValues(keyName);

		if ((results == null) || (results.length == 0)) {
			LOG.info("No resource found with key \"" + keyName + "\"!");
		}

		return results;
	}

	@RequestMapping("/logout/")
	@ResponseBody
	void logout() {
		LOG.info("called /logout/");

		collector.setAccounts(new AwsAccount[0]);

		LOG.info("cleared accounts");
	}

	@RequestMapping("/regions/")
	@ResponseBody
	Regions[] regions() {
		LOG.info("called /regions/");

		return collector.getStats().getRegions();
	}

	@RequestMapping("/regions/{region}/")
	@ResponseBody
	AwsResource[] regions(@PathVariable String region) {
		LOG.info("called /regions/" + region + "/");

		AwsResource[] results = collector.getStats().getResourcesByRegion(Regions.valueOf(region));

		if ((results == null) || (results.length == 0)) {
			LOG.info("No resource found for region \"" + region + "\"!");
		}

		return results;
	}

	@RequestMapping("/resources/")
	@ResponseBody
	AwsStats resources() {
		LOG.info("called /resources/");

		return collector.getStats();
	}

	@RequestMapping("/resources/{resourceName}/")
	@ResponseBody
	AwsResource resources(@PathVariable String resourceName) {
		LOG.info("called /resources/" + resourceName + "/");

		AwsResource res = collector.getStats().getResource(resourceName);

		if (res == null) {
			LOG.info("No resource found with name \"" + resourceName + "\"!");
		}

		return res;
	}

	@RequestMapping("/search/{searchPattern}/")
	@ResponseBody
	AwsResource[] search(@PathVariable String searchPattern) {
		searchPattern = decodeParam(searchPattern);
		LOG.info("called /search/" + searchPattern + "/");

		AwsResource[] results = collector.getStats().searchResources(searchPattern);

		if ((results == null) || (results.length == 0)) {
			LOG.info("No resource found with pattern \"" + searchPattern + "\"!");
		}

		return results;
	}

	@RequestMapping("/statistics/")
	@ResponseBody
	String statistics() {
		LOG.info("called /statistics/");

		AwsStats stats = collector.getStats();
		StringBuilder s = new StringBuilder();
		StatsTable t = new StatsTable();

		AwsResource[] resources = stats.getResources();
		String[] accounts = stats.getAccounts();
		Regions[] regions = stats.getRegions();
		AwsResourceType[] resourceTypes = stats.getUsedResourceTypes();
		String[] teams = stats.getTeams();
		String[] apps = stats.getApps();
		String[] publicApps = stats.getPublicApps();
		String[] instanceTypes = stats.getUsedEC2InstanceTypes();
		String[] amis = stats.getUsedAMIs();
		AwsResource[] ec2instances = stats.getResources(AwsResourceType.EC2);
		AwsStatsSummary summary = stats.getSummary();

		s.append("<html><header><style>p, li, ul, a { font-family:'Courier New', Arial; }</style></header><body><h1>AWS Utilization Statistics</h1><p>"
				+ "<a href=/>Back to overview</a><ul><li><a href=/resources/>" + resources.length + "</a> resources used</li>");

		/*
		 * accounts
		 */
		s.append("<li><a href=/accounts/>" + accounts.length + "</a> accounts</li><ul>");
		t.clear();

		for (String accountName : accounts) {
			int amount = stats.getResourcesByAccount(accountName).length;
			t.add(amount, "<li><a href=/accounts/" + encodeParam(accountName) + "/>" + amount + "</a> resources by \"" + accountName + "\"</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * regions
		 */
		s.append("<li><a href=/regions/>" + regions.length + "</a> regions</li><ul>");
		t.clear();

		for (Regions region : regions) {
			int amount = stats.getResourcesByRegion(region).length;
			t.add(amount, "<li><a href=/regions/" + region + "/>" + amount + "</a> resources in \"" + region + "\"</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * resource types
		 */
		s.append("<li>" + resourceTypes.length + " AWS components used</li><ul>");
		t.clear();

		for (AwsResourceType resourceType : resourceTypes) {
			int amount = stats.getResources(resourceType).length;
			String text = resourceType.toString();

			if ((resourceType == AwsResourceType.S3) && (summary.getS3Objects() > 0)) {
				text = resourceType.toString() + "(data size: " + AwsStatsSummary.readableFileSize(summary.getS3DataSizeInBytes()) + ", objects: "
						+ AwsStatsSummary.readableLong(summary.getS3Objects()) + ")";
			}

			t.add(amount, "<li><a href=/types/" + resourceType + "/>" + amount + "</a> " + text + "</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * teams
		 */
		s.append("<li><a href=/keys/Team/>" + teams.length + "</a> teams</li><ul>");
		t.clear();

		for (String teamName : teams) {
			int amount = stats.getResourcesByTeam(teamName).length;
			t.add(amount, "<li><a href=/values/Team/" + encodeParam(teamName) + "/>" + amount + "</a> resources by \"" + teamName + "\"</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * EC2 apps
		 */
		s.append("<li><a href=/apps/>" + apps.length + "</a> EC2 based apps using <a href=/types/EC2/>" + ec2instances.length + "</a> EC2 instances</li>"
				+ "<ul>");
		t.clear();

		for (String appName : apps) {
			int amount = stats.getAppInstances(appName).length;
			t.add(amount, "<li><a href=/apps/" + encodeParam(appName) + "/>" + amount + "</a> instances of \"" + appName + "\"</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * Apps that are externally reachable
		 */
		s.append("<li><a href=/keys/PublicDnsName/>" + publicApps.length + "</a> EC2 based apps are externally reachable (public dns name)</li>" + "<ul>");
		t.clear();

		for (String appName : publicApps) {
			int amount = stats.getAppInstances(appName).length;
			t.add(amount, "<li><a href=/apps/" + encodeParam(appName) + "/>" + amount + "</a> instances of \"" + appName + "\"</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * Instances that are running since > 30 days
		 */
		int maxDays = 30;
		int runningSinceDays = 0;
		List<String> names = new ArrayList<String>();
		t.clear();
		for (AwsResource res : stats.getResourcesRunningSince(maxDays)) {
			String name = res.getName();
			if (!names.contains(name)) {
				int days = (int) res.get(AwsTag.RunningSinceDays);
				t.add(days, "<li>" + days + " days: <a href=/resources/" + encodeParam(name) + "/>" + name + "</a></li>");
				names.add(name);
			}
			runningSinceDays++;
		}

		s.append("<li>" + runningSinceDays + " EC2 instances are running since > " + maxDays + " days</li>" + "<ul>");

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * EC2 instance types
		 */
		s.append("<li><a href=/instancetypes/>" + instanceTypes.length + "</a> used EC2 instance types by <a href=/types/EC2/>" + ec2instances.length
				+ "</a> EC2 instances</li><ul>");
		t.clear();

		for (String instanceType : instanceTypes) {
			int amount = stats.getResourcesByEC2InstanceType(instanceType).length;
			t.add(amount, "<li><a href=/instancetypes/" + encodeParam(instanceType) + "/>" + amount + "</a> instances with \"" + instanceType + "\"</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		/*
		 * AMIs
		 */
		s.append("<li><a href=/amis/>" + amis.length + "</a> used EC2 AMIs by <a href=/types/EC2/>" + ec2instances.length + "</a> EC2 instances</li><ul>");
		t.clear();

		for (String ami : amis) {
			int amount = stats.searchResources(AwsTag.AMI, ami).length;
			t.add(amount, "<li><a href=/amis/" + encodeParam(ami) + "/>" + amount + "</a> instances with AMI \"" + ami + "\"</li>");
		}

		s.append(t.printSorted());
		s.append("</ul>");

		s.append("</ul></p></body></html>");

		return s.toString();
	}

	@RequestMapping("/summary/")
	@ResponseBody
	AwsStatsSummary summary() {
		LOG.info("called /summary/");

		return collector.getStats().getSummary();
	}

	@RequestMapping("/teams/")
	@ResponseBody
	String[] teams() {
		LOG.info("called /teams/");

		return collector.getStats().getTeams();
	}

	@RequestMapping("/teams/{teamName}/")
	@ResponseBody
	AwsResource[] teams(@PathVariable String teamName) {
		teamName = decodeParam(teamName);
		LOG.info("called /teams/" + teamName + "/");

		AwsResource[] results = collector.getStats().getResourcesByTeam(teamName);

		if ((results == null) || (results.length == 0)) {
			LOG.info("No resource found for team \"" + teamName + "\"!");
		}

		return results;
	}

	@RequestMapping("/test/")
	@ResponseBody
	AwsStats test() {
		LOG.info("called /test/");

		collector.generateSampleData(30);

		return collector.getStats();
	}

	@RequestMapping("/test/{maxItems}/")
	@ResponseBody
	AwsStats test(@PathVariable int maxItems) {
		LOG.info("called /test/" + maxItems + "/");

		collector.generateSampleData(maxItems);

		return collector.getStats();
	}

	@RequestMapping("/types/")
	@ResponseBody
	AwsResourceType[] types() {
		LOG.info("called /types/");

		return collector.getStats().getUsedResourceTypes();
	}

	@RequestMapping("/types/{resourceType}/")
	@ResponseBody
	AwsResource[] types(@PathVariable String resourceType) {
		LOG.info("called /types/" + resourceType + "/");

		AwsResource[] results = collector.getStats().getResources(AwsResourceType.valueOf(resourceType));

		if ((results == null) || (results.length == 0)) {
			LOG.info("No resource found of type \"" + resourceType + "\"!");
		}

		return results;
	}

	@RequestMapping("/values/{key}/{value}/")
	@ResponseBody
	AwsResource[] values(@PathVariable String key, @PathVariable String value) {
		value = decodeParam(value);
		LOG.info("called /values/" + key + "/" + value + "/");

		AwsResource[] results = collector.getStats().searchResources(key, value);

		if ((results == null) || (results.length == 0)) {
			LOG.info("No resource found with key \"" + key + "\" and value \"" + value + "\"!");
		}

		return results;
	}
}