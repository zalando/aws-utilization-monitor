/**
 * 
 */
package de.zalando.platform.awsutilizationmonitor.api;

/**
 * @author jloeffler
 *
 */
public enum AwsResourceType {
	Unknown,
	
	DynamoDB,
	EC2,
	ElastiCache,
	ElasticMapReduce,
	ElasticTranscoder,
	Kinesis,
	RDS,
	Redshift,
	S3,
	SimpleDB,
	SNS,
	SQS
}