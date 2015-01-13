/**
 * 
 */
package de.zalando.platform.awsutilizationmonitor.api;

/**
 * @author jloeffler
 *
 */
public class AwsStatsSummary {
    private int apps;    
    private int owners;
	private int regions;
	private int resourceTypes;
	private int resources;
	private int teams;
	
	/**
	 * @return the apps
	 */
	public int getApps() {
		return apps;
	}
	/**
	 * @return the owners
	 */
	public int getOwners() {
		return owners;
	}
	/**
	 * @return the regions
	 */
	public int getRegions() {
		return regions;
	}
	/**
	 * @return the resources
	 */
	public int getResources() {
		return resources;
	}
	/**
	 * @return the resourceTypes
	 */
	public int getResourceTypes() {
		return resourceTypes;
	}
	/**
	 * @return the teams
	 */
	public int getTeams() {
		return teams;
	}
	/**
	 * @param apps the apps to set
	 */
	public void setApps(int apps) {
		this.apps = apps;
	}
	/**
	 * @param owners the owners to set
	 */
	public void setOwners(int owners) {
		this.owners = owners;
	}
	/**
	 * @param regions the regions to set
	 */
	public void setRegions(int regions) {
		this.regions = regions;
	}
	/**
	 * @param resources the resources to set
	 */
	public void setResources(int resources) {
		this.resources = resources;
	}
	/**
	 * @param resourceTypes the resourceTypes to set
	 */
	public void setResourceTypes(int resourceTypes) {
		this.resourceTypes = resourceTypes;
	}
	/**
	 * @param teams the teams to set
	 */
	public void setTeams(int teams) {
		this.teams = teams;
	}
}