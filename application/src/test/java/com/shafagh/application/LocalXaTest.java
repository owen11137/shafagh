package com.shafagh.application;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LocalXaTest extends AtomicityChecks {
 @Autowired Environment environment;
 @Test void httpCustomerAccountAndCreditWorkflow() throws Exception {
  String customer=id(post("/api/customers", "{\"name\":\"HTTP demo\"}"));
  String account=id(post("/api/accounts", "{\"customerId\":\""+customer+"\"}"));
  String response=post("/api/accounts/"+account+"/credits", "{\"operationId\":\""+UUID.randomUUID()+"\",\"amount\":100.00}");
  assertThat(response).contains("\"balance\":100.00");
 }
 private String post(String path,String body) throws Exception {
  var request=HttpRequest.newBuilder(URI.create("http://localhost:"+environment.getRequiredProperty("local.server.port")+path))
   .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
  var response=HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
  assertThat(response.statusCode()).isEqualTo(200);
  return response.body();
 }
 private String id(String json) {
  var match=java.util.regex.Pattern.compile("\"id\":\"([^\"]+)\"").matcher(json);
  assertThat(match.find()).isTrue();return match.group(1);
 }
}
