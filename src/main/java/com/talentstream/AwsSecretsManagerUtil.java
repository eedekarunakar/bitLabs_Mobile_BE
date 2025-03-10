package com.talentstream;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsSecretsManagerUtil {
	
	 private static final String ACCESS_KEY = System.getenv("AWS_ACCESS_KEY");
	  private static final String SECRET_KEY = System.getenv("AWS_SECRET_KEY");
	  private static final String REGION = System.getenv("AWS_REGION");
	  private static final String DATABASE_SECRET_NAME=System.getenv("AWS_DATABASE_SECRET_NAME");

	public static String getSecret() {
		  try {
		  String secrets = System.getenv("AWS_ACCESS_KEY_ID");
		  System.out.println("Secretes  "+secrets);
		  JSONObject jsonObject = new JSONObject(secrets);
	        String accessKey = jsonObject.getString("AWS_ACCESS_KEY_ID");
	        String secretKey = jsonObject.getString("AWS_SECRET_ACCESS_KEY");
	        String region1 = jsonObject.getString("AWS_REGION");
	        Region region = Region.of(region1);
	        
	        
	        if (accessKey == null || secretKey == null) {
	            System.err.println("AWS credentials are not set in environment variables.");
	            return null;
	        }
	
	            return secrets;
	        } catch (Exception e) {
	            System.err.println("An error occurred: " + e.getMessage());
	            return null;
	        }
    }
	 
	 //added method to get Database secret key with all required values like username ,password 
	public static String getDBSecret() {
		try {
          if (ACCESS_KEY == null || SECRET_KEY == null || REGION == null) {
              System.err.println("AWS credentials or region are not set in environment variables.");
              return null;
          }
          SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                  .region(Region.of(REGION))
                  .credentialsProvider(StaticCredentialsProvider.create(
                          AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
                  ))
                  .build();

			
          GetSecretValueRequest request = GetSecretValueRequest.builder()
                  .secretId(DATABASE_SECRET_NAME)
                  .build();

        
          GetSecretValueResponse response = secretsClient.getSecretValue(request);
         
          return response.secretString();
			
		} catch (Exception e) {
			System.err.println("An error occurred: " + e.getMessage());
			return null;
		}
	}
	public String getDbUsername() {
		String secret = getDBSecret();
		if (secret == null) {
			return null;
		}

		JSONObject jsonObject = new JSONObject(secret);
		System.out.println("Username is : "+jsonObject.getString("username"));
		return jsonObject.getString("username");
	}

	public String getDbPassword() {
		String secret = getDBSecret();
		if (secret == null) {
			return null;
		}

		JSONObject jsonObject = new JSONObject(secret);
		System.out.println("Password is : "+jsonObject.getString("password"));
		return jsonObject.getString("password");
	}
	
	

    
}
