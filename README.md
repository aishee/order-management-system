##Build

mvn spring-boot:run -Dspring.profiles.active=local

http://localhost:8080/actuator/health

http://localhost:8080/swagger-ui.html

## Available Badges from other systems
<table>
  <tr>
    <td>    
      <a href="https://ci.walmart.com/job/Build_and_Deploy/job/ukgr_order-management-system/" target="_blank">
        <img src="https://ci.walmart.com/buildStatus/icon?job=Build_and_Deploy%2Fukgr_order-management-system">
      </a>
    </td>
     <td>    
          <a href="https://sonar.looper.prod.walmartlabs.com/dashboard?id=com.walmart.ukgr.oms%3Aorder-management-system" target="_blank">
            <img src="https://sonar.looper.prod.walmartlabs.com/api/project_badges/measure?project=com.walmart.ukgr.oms%3Aorder-management-system&metric=coverage">
          </a>
        </td>
    <td>
      <a href="https://oneops.prod.walmart.com/ukgrsps/assemblies/ukgr-oms#summary" target="_blank">
        <img src="https://oneops.prod.walmart.com/assets/logo-7544c56450ef15a2492b5d270d2c82d8.png" width="110" height="21">
      </a>
    </td>
    <td>
      <a href="https://hygieia.walmart.com/#/dashboard/5eebda24d5aecf7b79ef8dbc" target="_blank">
        <img src="https://hygieia.walmart.com/assets/img/hygieia_b.png" width="110" height="25">
      </a>
    </td>
     <td>
      <a href="https://sonar.looper.prod.walmartlabs.com/dashboard?id=com.walmart.ukgr.oms%3Aorder-management-system" target="_blank">
        <img src="https://sonar.looper.prod.walmartlabs.com/api/project_badges/measure?project=com.walmart.ukgr.oms%3Aorder-management-system&metric=alert_status" height="25">
      </a>
    </td>
  </tr>
</table>

## Important Urls

| Resources  | Information |
| ------------- | ------------- |
| Technologies | Reactive Java,Spring-boot,WebClient,REST,Kafka streams,SQL Server,Swagger,JMS |
| Architecture | https://confluence.walmart.com/pages/viewpage.action?pageId=366997288 https://confluence.walmart.com/display/~y0b007t/High+Level+Design |
| API Docs | https://confluence.walmart.com/display/ASDAECOMM/New+OMS+API+SPEC |
| Release Calendar | https://confluence.walmart.com/display/ASDAECOMM/IMP+Release+Playbook+Parent+Page |
| Swagger | https://order-management-service-app.qa.ukgr-oms.ukgrsps.prod.us.walmart.net/swagger-ui.html |
| OneOps | https://oneops.prod.walmart.com/ukgrsps/assemblies/ukgr-oms/transition/environments/1042036748#summary |
| Jenkins Dev | https://ci.walmart.com/job/Build_and_Deploy/job/ukgr_order-management-system/  |
| Jenkins Automation | https://ci.falcon.walmart.com/job/ODS/job/FMS%20Sanity/  |
| Sonar | https://sonar.looper.prod.walmartlabs.com/dashboard?id=com.walmart.ukgr.oms%3Aorder-management-system |
| Hygieia | https://hygieia.walmart.com/#/dashboard/5eebda24d5aecf7b79ef8dbc |
| Concord | https://concord.prod.walmart.com/#/org/ASDA/project/order-management-system/repository |
| Kafka Lenses | http://kafka.kafka-asda-ods-az-prod.prod-southcentralus-az.prod.us.walmart.net:9000/clusters/prod-southcentralus-az |
| Splunk Prod | https://asda-mls.prod.walmart.com/en-US/app/asda/search?q=search%20index%3D%22omsapp%22 |
| Splunk QA | https://asda-mls.prod.walmart.com/en-US/app/asda/search?q=search%20index%3D%22omsapp_qa%22 |
| DevOps Monitoring | https://vip-viewer.prod.walmart.com/oneops/#!/ukgrsps/ukgr-oms/prod/order-management-service-app |
| Grafana Application Metrics | https://grafana.mms.walmart.net/d/ordermans654/asda-order-management-application-metrics?orgId=1&refresh=1m&from=now-30m&to=now |
| Grafana System Metrics | https://grafana.mms.walmart.net/d/ordermans7894/asda-order-management-system-metrics?orgId=1&refresh=1m |
| Grafana Resiliency Dashboard| https://grafana.mms.walmart.net/d/ordermans9045/asda-order-management-resilience4j-metrics?orgId=1&refresh=1m |
| Grafana SQL Server| https://grafana.mms.walmart.net/d/jaK73zEZk/sql-server-dashboard?orgId=1&refresh=1m&from=now-6h&to=now&var-datasource=production&var-servcomm=ukgroms-db-production%20-%20prod&var-node=haz15977904931&var-product_id=754&var-inventory_datasource=production |