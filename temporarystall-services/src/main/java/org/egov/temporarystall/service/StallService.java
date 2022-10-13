package org.egov.temporarystall.service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.egov.temporarystall.common.CommonConstants;
import org.egov.temporarystall.config.StallConfiguration;
import org.egov.temporarystall.idgen.model.IdGenerationResponse;
import org.egov.temporarystall.model.Payment;
import org.egov.temporarystall.model.ResponseInfoWrapper;
import org.egov.temporarystall.model.StallApplication;
import org.egov.temporarystall.model.StallApplicationDocument;
import org.egov.temporarystall.model.StallRequest;
import org.egov.temporarystall.model.demand.Demand;
import org.egov.temporarystall.model.demand.Demand.StatusEnum;
import org.egov.temporarystall.producer.Producer;
import org.egov.temporarystall.model.demand.DemandDetail;
import org.egov.temporarystall.model.demand.DemandRequest;
import org.egov.temporarystall.repository.TemporaryStallRepository;
import org.egov.temporarystall.repository.ServiceRequestRepository;
import org.egov.temporarystall.util.AuditDetailsUtil;
import org.egov.temporarystall.util.IdGenRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@Service
public class StallService {

	private final ObjectMapper objectMapper;
	
	private StallConfiguration config;
	
	private TemporaryStallRepository repository;
	
	private IdGenRepository idgenrepository;
	
	private AuditDetailsUtil auditDetailsUtil;
	
	@Autowired
	private MDMSService mdmsService;
	
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private Producer producer;
	
	
	@Autowired
	private ServiceRequestRepository repository1;
	
	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	public StallService(TemporaryStallRepository repository,ObjectMapper objectMapper,IdGenRepository idgenrepository,
			StallConfiguration config,AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.idgenrepository = idgenrepository;
		this.config = config;
		this.auditDetailsUtil=auditDetailsUtil;
	}

	public ResponseEntity<ResponseInfoWrapper> createStallApplication(StallRequest stallrequest) {
		try {
			StallApplication stallapplication = objectMapper.convertValue(stallrequest.getStallApplicationRequest(),
					StallApplication.class);
			String stallid = UUID.randomUUID().toString();
			stallapplication.setApplicationUuid(stallid);
			stallapplication.setIsActive(true);
			stallapplication.setAuditDetails(
					auditDetailsUtil.getAuditDetails(stallrequest.getRequestInfo(), CommonConstants.ACTION_DRAFT));
			// idgen service call to genrate event id
			IdGenerationResponse id = idgenrepository.getId(stallrequest.getRequestInfo(), stallapplication.getTenantId(),
					config.getStallapplicationNumberIdgenName(), config.getStallapplicationNumberIdgenFormat(), 1);
			if (id.getIdResponses() != null && id.getIdResponses().get(0) != null)
				stallapplication.setApplicationId(id.getIdResponses().get(0).getId());
			else
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.ID_GENERATION);

			//stallapplication.setTotalamount(stallapplication.getNoofdays() * stallapplication.getFeesperday());
			stallapplication.setApplicationstatus(CommonConstants.ACTION_DRAFT);
			// save document to temporary_stall_application_document table
			List<StallApplicationDocument> stalldoc = new ArrayList<>();
			for (StallApplicationDocument docobj : stallapplication.getApplicationDocument()) {
				StallApplicationDocument document = new StallApplicationDocument();
				document.setDocumnetUuid(UUID.randomUUID().toString());
				document.setApplicationUuid(stallid);
				document.setDocumentType(docobj.getDocumentType());
				document.setFilestoreId(docobj.getFilestoreId());
				document.setAuditDetails(
						auditDetailsUtil.getAuditDetails(stallrequest.getRequestInfo(), CommonConstants.ACTION_DRAFT));
				document.setIsActive(true);
				document.setTenantId(stallapplication.getTenantId());
				stalldoc.add(document);

			}

			stallapplication.setApplicationDocument(stalldoc);
			
			
			Object mdmsData = mdmsService.mDMSCall(stallrequest.getRequestInfo(), stallapplication.getTenantId());
			
//			int getpayperrant = getpayperrant(stallrequest.getRequestInfo(), mdmsData,stallapplication);
			
			double stallsizerate = getStallsize(stallrequest.getRequestInfo(), mdmsData,stallapplication);
			
			int noofdays = stallapplication.getNoofdays();
			
			if(stallapplication.getFestival().equalsIgnoreCase("Diwali")) {
			
				
				
				if((noofdays > 3) && (noofdays <= 6) ) {
					noofdays = 7 ;
				}
				else if ((noofdays > 7) && (noofdays <= 19)) {
					noofdays = 20 ;
				}
				
			}
          
			
            double gstrate = getGstRate(stallrequest.getRequestInfo(), mdmsData,stallapplication);
           
            double amount ;
			
			double gstamount;

			if(stallapplication.getFestival().equalsIgnoreCase("Diwali")) {
            
				 amount= stallsizerate * noofdays;
				
				 gstamount= gstrate * noofdays;
			}
			
			else {
				 amount= stallsizerate * stallapplication.getNoofdays();
				
				 gstamount= gstrate * stallapplication.getNoofdays();
				
			}
            
			
			
			double totalamount= amount + gstamount;
	
	
		    stallapplication.setAmount(amount);
			
			stallapplication.setGstamount(gstamount);			
			
			//double amount=stallapplication.getNoofdays() * stallsizerate ;
			
			//double totalamt =  amount + (amount * 0.18);
			
			//double totalamount = Math.round(totalamt + 0.4);
			
			
			
			stallapplication.setTotalamount(totalamount);
			
			
			enrichmentService.generateDemand(stallrequest);
			
			repository.createSTALLApplication(stallapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(stallapplication).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.STALL_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	
	
	
	public ResponseEntity<ResponseInfoWrapper> getSTALLApplication(StallRequest stallrequest) {
		try {

			StallApplication StallApplication = objectMapper.convertValue(stallrequest.getStallApplicationRequest(),
					StallApplication.class);
			
			
			if (StallApplication.getApplicationId() != null) {
				String updatePaymentStatus = updatePaymentStatus(StallApplication);
				
				StallApplication.setPaymentstatus(updatePaymentStatus);
				
				StallRequest infoWrapper = StallRequest.builder().stallApplicationRequest(StallApplication).build();
				
				producer.push(config.getSTALLApplicationUpdatepaymentstatusTopic(), infoWrapper );
			}
			
//			updatePaymentStatus();
			
			List<StallApplication> StallApplicationresult = repository.getStallApplication(StallApplication);
			
			if(StallApplication.getMobileno() != null) {
			for (StallApplication stallApplication : StallApplicationresult) {
				if(stallApplication.getApplicationId() != null) {
					String updatePaymentStatus = updatePaymentStatus(stallApplication);
					
					StallApplication.setPaymentstatus(updatePaymentStatus);
					
					StallRequest infoWrapper = StallRequest.builder().stallApplicationRequest(StallApplication).build();
					
					producer.push(config.getSTALLApplicationUpdatepaymentstatusTopic(), infoWrapper );
					
				}
			}
			}
			
			List<StallApplication> StallApplicationresultfinal = repository.getStallApplication(StallApplication);
			
//			if(StallApplicationresult.size() == 1)
//			StallApplicationresult.get(0).setPaymentstatus(updatePaymentStatus);
			
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(StallApplicationresultfinal).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.STALL_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	


private String updatePaymentStatus(StallApplication stallApplication) {
	List<StallApplication> stallPaymentStatusDB = repository.getStallPaymentStatus(stallApplication);
	String status = null ;
	List<String> ll = new ArrayList<>();
	for (int i = 0; i < stallPaymentStatusDB.size(); i++) {
		ll.add(stallPaymentStatusDB.get(i).getPaymentstatus());
		
	}
	if (ll.contains("PENDING")) {
		return status = "PENDING" ;
	}
	else if (ll.contains("FAILURE") || ll.contains("SUCCESS")  ) {
		if(ll.contains("SUCCESS")) {
			return status = "SUCCESS" ;
		}
		return status = "FAILURE" ;
	} 
	else if (ll.contains("SUCCESS") ) {
		return status = "SUCCESS" ;
	}
	return status ;
	}
	

	
	@SuppressWarnings("unlikely-arg-type")
	public int getStallsize(RequestInfo requestInfo,Object mdmsData,StallApplication stallapplication) {
	int sizeamount = 0;
	try {

	LinkedHashMap opmsData = JsonPath.read(mdmsData, CommonConstants.MDMS_PM_PATH);
	if (opmsData.size() == 0)
	return 0;

	List jsonOutput = JsonPath.read(mdmsData, CommonConstants.MDMS_TAXHEAD_SIZE_PATH);
	
List jsonOutput1 = JsonPath.read(mdmsData, CommonConstants.MDMS_TAXHEAD_STALL_CONFIG_PATH);
	
	for (Object entry : jsonOutput1) {
		HashMap<String, Object> map = (HashMap<String, Object>) entry;

		String value = ((String) map.get("value"));
		
		if(value!=null && value.equalsIgnoreCase(stallapplication.getStallsize())) {

			sizeamount = new Integer(map.get("amount").toString());
			
			return sizeamount;
		
		}		
		}


	for (Object entry : jsonOutput) {
	HashMap<String, Object> map = (HashMap<String, Object>) entry;


	String sector = ((String) map.get("sector")).split("\\.")[0];
	String size = ((String) map.get("size"));
//	String requestdays = ((String) map.get("maxdays"));


	/*
	 * if("Diwali".equalsIgnoreCase(stallapplication.getFestival()) &&
	 * stallapplication.getNoofdays()== 20) {
	 * if("Up to 250Sq.ft".equalsIgnoreCase(stallapplication.getStallsize())){
	 * sizeamount = 2000;
	 * 
	 * break; } }
	 */
	if(sector!=null && (!"22A".equalsIgnoreCase(stallapplication.getSector()) && !"22B".equalsIgnoreCase(stallapplication.getSector()))) {
	if(size !=null && size.equalsIgnoreCase(stallapplication.getStallsize())){
	sizeamount = new Integer(map.get("rate").toString());

	break;
	}
	}
	if(sector.equalsIgnoreCase(stallapplication.getSector())) {
	if(size !=null && size.equalsIgnoreCase(stallapplication.getStallsize())) {
	sizeamount = new Integer(map.get("rate").toString());

	break;
	}
	}

	}


	} catch (Exception e) {
	throw new CustomException("MDMS ERROR", "Failed to get calculationType");
	}

	return sizeamount;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public int getGstRate(RequestInfo requestInfo,Object mdmsData,StallApplication stallapplication) {
	int gstamount = 0;
	try {

	LinkedHashMap opmsData = JsonPath.read(mdmsData, CommonConstants.MDMS_PM_PATH);
	if (opmsData.size() == 0)
	return 0;

	List jsonOutput = JsonPath.read(mdmsData, CommonConstants.MDMS_TAXHEAD_SIZE_PATH);
	
    List jsonOutput1 = JsonPath.read(mdmsData, CommonConstants.MDMS_TAXHEAD_STALL_CONFIG_PATH);
	
	for (Object entry : jsonOutput1) {
		HashMap<String, Object> map = (HashMap<String, Object>) entry;

		String value = ((String) map.get("value"));
		
		if(value!=null && value.equalsIgnoreCase(stallapplication.getStallsize())) {
			gstamount = new Integer(map.get("gstrate").toString());
			
			return gstamount;
		
		}		
		}


	for (Object entry : jsonOutput) {
	HashMap<String, Object> map = (HashMap<String, Object>) entry;


	String sector = ((String) map.get("sector")).split("\\.")[0];
	String size = ((String) map.get("size"));
//	String requestdays = ((String) map.get("maxdays"));


	/*
	 * if("Diwali".equalsIgnoreCase(stallapplication.getFestival()) &&
	 * stallapplication.getNoofdays()== 20) {
	 * if("Up to 250Sq.ft".equalsIgnoreCase(stallapplication.getStallsize())){
	 * sizeamount = 2000;
	 * 
	 * break; } }
	 */
	if(sector!=null && (!"22A".equalsIgnoreCase(stallapplication.getSector()) && !"22B".equalsIgnoreCase(stallapplication.getSector()))) {
	if(size !=null && size.equalsIgnoreCase(stallapplication.getStallsize())){
		gstamount = new Integer(map.get("gstrate").toString());

	break;
	}
	}
	if(sector.equalsIgnoreCase(stallapplication.getSector())) {
	if(size !=null && size.equalsIgnoreCase(stallapplication.getStallsize())) {
		gstamount = new Integer(map.get("gstrate").toString());

	break;
	}
	}

	}


	} catch (Exception e) {
	throw new CustomException("MDMS ERROR", "Failed to get calculationType");
	}

	return gstamount;
	}
	
	
	///
	
	public ResponseEntity<ResponseInfoWrapper> updateStallApplication(StallRequest stallrequest) {
		StallApplication StallApplication = objectMapper.convertValue(stallrequest.getStallApplicationRequest(),
				StallApplication.class);			
		
//		validate.validateStall(StallApplication);
		StallApplication.setAuditDetails(
				auditDetailsUtil.getAuditDetails(stallrequest.getRequestInfo(), CommonConstants.ACTION_DRAFT));
		StallApplication.setTotalamount(StallApplication.getNoofdays() * StallApplication.getFeesperday());
		StallApplication.setApplicationstatus(CommonConstants.ACTION_DRAFT);
		// Update document to temporary_stall_application_document table
					List<StallApplicationDocument> stalldoc = new ArrayList<>();
					for (StallApplicationDocument docobj : StallApplication.getApplicationDocument()) {
						StallApplicationDocument document = new StallApplicationDocument();
						document.setDocumnetUuid(docobj.getDocumnetUuid());
						document.setDocumentType(docobj.getDocumentType());
						document.setApplicationUuid(docobj.getApplicationUuid());
						document.setFilestoreId(docobj.getFilestoreId());
						document.setAuditDetails(
								auditDetailsUtil.getAuditDetails(stallrequest.getRequestInfo(), CommonConstants.ACTION_DRAFT));
						document.setIsActive(true);
						document.setTenantId(StallApplication.getTenantId());
						stalldoc.add(document);

					}
					
					
					Object mdmsData = mdmsService.mDMSCall(stallrequest.getRequestInfo(), StallApplication.getTenantId());
					
					double stallsizerate = getStallsize(stallrequest.getRequestInfo(), mdmsData,StallApplication);
					
					 double gstrate = getGstRate(stallrequest.getRequestInfo(), mdmsData,StallApplication);
						
//						double amount= stallsizerate * StallApplication.getNoofdays();
//						
//						double gstamount= gstrate * StallApplication.getNoofdays();
					 
					 int noofdays = StallApplication.getNoofdays();
					 
					 if(StallApplication.getFestival().equalsIgnoreCase("Diwali")) {
							
							
							if((noofdays > 3) && (noofdays <= 6) ) {
								noofdays = 7 ;
							}
							else if ((noofdays > 7) && (noofdays <= 19)) {
								noofdays = 20 ;
							}
							
						}
					 
					  double amount ;
						
						double gstamount;

						if(StallApplication.getFestival().equalsIgnoreCase("Diwali")) {
			            
							 amount= stallsizerate * noofdays;
							
							 gstamount= gstrate * noofdays;
						}
						
						else {
							 amount= stallsizerate * StallApplication.getNoofdays();
							
							 gstamount= gstrate * StallApplication.getNoofdays();
							
						}
			            
						
						
//						double totalamount= amount + gstamount;
				
				
						StallApplication.setAmount(amount);
						
						StallApplication.setGstamount(gstamount);
						
						double totalamount = amount + gstamount;
				
				
// 						StallApplication.setAmount(amount);
						
// 						StallApplication.setGstamount(gstamount);	
					
					/*
					 * double amount=StallApplication.getNoofdays() * stallsizerate ;
					 * 
					 * double totalamt = amount + (amount * 0.18);
					 * 
					 * double totalamount = Math.round(totalamt + 0.4);
					 */
					
					StallApplication.setTotalamount(totalamount);
					
					
					StringBuilder uri = new StringBuilder();
					uri = uri.append(config.getBillingHost()).append(config.getBillingHostSerach());
					
					uri = uri.append("tenantId=").append("ch.chandigarh").append("&businessService=").append("TEMPORARY_STALL_CHARGES_BOOKING&consumerCode=").append(StallApplication.getApplicationId());
					StallRequest.builder().requestInfo(stallrequest.getRequestInfo()).build();
					Object fetchResult = repository1.fetchResult(uri,
							stallrequest.getRequestInfo());   
					
					
					
					
					StringBuilder curi = new StringBuilder();
					StringBuilder append = curi.append(config.getCollectionHostSerach()).append(config.getCollectionSearcheUrl());
					append.append("consumerCodes=").append(StallApplication.getApplicationId()).append("&tenantId=ch.chandigarh");
					


					StringBuilder curii = new StringBuilder();

// 					StallRequest build3 = StallRequest.builder().requestInfo(stallrequest.getRequestInfo()).build();
// 					Object fetchResult2 = repository1.fetchResult(append, build3);

					
// 					Payment response3= mapper.convertValue(fetchResult2, Payment.class);
// 					System.out.println(response3.getTotalAmountPaid());
// 					if(response3.getTotalAmountPaid() !=  null && response3.getTotalDue() != null){
// 						if(response3.getTotalAmountPaid()==response3.getTotalDue()) {
// 							StallApplication.setApplicationstatus(CommonConstants.ACTION_PAYMENT);
// 						}
// 						else {
// 							StallApplication.setApplicationstatus(CommonConstants.ACTION_DRAFT);
// 						}
						
// 					}
// 					else {
// 						StallApplication.setApplicationstatus(CommonConstants.ACTION_DRAFT);
// 					}
					

					
			StallApplication stallDemand = repository.getStallDemand(StallApplication);
			
			List<StallApplication> stallDemandId = repository.getStallDemandDetailId(StallApplication);
			
			
					
						
						org.egov.common.contract.request.User user = new org.egov.common.contract.request.User();

//						User lp = stallrequest.getRequestInfo().getUserInfo();
						user.setId(stallrequest.getRequestInfo().getUserInfo().getId());
						user.setEmailId(stallrequest.getRequestInfo().getUserInfo().getEmailId());
						user.setName(stallrequest.getRequestInfo().getUserInfo().getName());
						user.setTenantId(stallrequest.getRequestInfo().getUserInfo().getTenantId());
						user.setType(stallrequest.getRequestInfo().getUserInfo().getType());
						user.setUuid(stallrequest.getRequestInfo().getUserInfo().getUuid());
						user.setUserName(stallrequest.getRequestInfo().getUserInfo().getUserName());

				
						double j =0;

						

						DemandDetail demanddetails = new DemandDetail();
						demanddetails.setId(stallDemandId.get(0).getDemaniddetailid());
						demanddetails.setTaxHeadMasterCode("TEMPORARY_STALL_CHARGES_BOOKING");
						demanddetails.setDemandId(stallDemand.getDemanid());
						demanddetails.setTenantId("ch.chandigarh");
						demanddetails.setCollectionAmount(j);
						demanddetails.setTaxAmount(amount);
						demanddetails.setAuditDetails(StallApplication.getAuditDetails());
						
						DemandDetail demanddetailsGst = new DemandDetail();
						demanddetailsGst.setId(stallDemandId.get(1).getDemaniddetailid());
						demanddetailsGst.setTaxHeadMasterCode("TEMPORARY_STALL_GST_CHARGES_BOOKING");
						demanddetailsGst.setDemandId(stallDemand.getDemanid());
						demanddetailsGst.setTenantId("ch.chandigarh");
						demanddetailsGst.setCollectionAmount(j);
						demanddetailsGst.setTaxAmount(gstamount);
						demanddetailsGst.setAuditDetails(StallApplication.getAuditDetails());

						List<DemandDetail> dema1 = new ArrayList<>();

						dema1.add(demanddetails);
						
						dema1.add(demanddetailsGst);

						List<Demand> dema = new ArrayList<>();
						Demand build2 = Demand.builder().id(stallDemand.getDemanid()).tenantId("ch.chandigarh")
								.consumerCode(StallApplication.getApplicationId()).consumerType("booking")
								.businessService("TEMPORARY_STALL_CHARGES_BOOKING")
								.taxPeriodFrom(StallApplication.getAuditDetails().getLastModifiedTime())
								.taxPeriodTo(StallApplication.getAuditDetails().getLastModifiedTime())
								.demandDetails(dema1).auditDetails(StallApplication.getAuditDetails())
								.minimumAmountPayable(BigDecimal.ZERO).status(StatusEnum.ACTIVE).payer(user)
								.demandDetails(dema1).build();
						dema.add(build2);
						DemandRequest DR = new DemandRequest();
						DemandRequest build = DR.builder().demands(dema).build();
						DemandRequest request = new DemandRequest(stallrequest.getRequestInfo(), dema);

						
						
						StallApplication.setApplicationstatus(updateApplicationStatus(StallApplication));
		
		if ((StallApplication.getPaymentstatus()==CommonConstants.FAILURE)  || (StallApplication.getPaymentstatus() == null)) {
							MdmsResponse response2 = mapper.convertValue(
									repository1.fetchResult(repository.getBillingUpdateUrl(), request),
									MdmsResponse.class);
						}
						
						
						
					StallApplication.setApplicationDocument(stalldoc);
					repository.updateSTALLApplication(StallApplication);
		
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.responseBody(StallApplication).build(), HttpStatus.CREATED);
		
		
	}

	private String updateApplicationStatus(StallApplication stallApplication) {
		List<StallApplication> stallPaymentStatusDB = repository.getStallPaymentStatus(stallApplication);
		String status = null ;
		List<String> ll = new ArrayList<>();
		for (int i = 0; i < stallPaymentStatusDB.size(); i++) {
			ll.add(stallPaymentStatusDB.get(i).getPaymentstatus());
			
		}
		if (ll.contains("PENDING")) {
			return status = CommonConstants.ACTION_DRAFT ;
		}
		else if (ll.contains("FAILURE") || ll.contains("SUCCESS")  ) {
			if(ll.contains("SUCCESS")) {
				return status = CommonConstants.ACTION_PAYMENT ;
			}
			return status = CommonConstants.ACTION_DRAFT ;
		} 
		else if (ll.contains("SUCCESS") ) {
			return status = CommonConstants.ACTION_PAYMENT ;
		}
		else {
			 status = CommonConstants.ACTION_DRAFT ;
		}
		return status ;
		}


}
