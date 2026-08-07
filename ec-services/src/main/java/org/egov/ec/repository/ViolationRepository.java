package org.egov.ec.repository;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.ec.config.EchallanConfiguration;
import org.egov.ec.producer.Producer;
import org.egov.ec.repository.builder.EcQueryBuilder;
import org.egov.ec.repository.rowmapper.ColumnsRowMapper;
import org.egov.ec.repository.rowmapper.ReceiptNoRowMapper;
import org.egov.ec.repository.rowmapper.ViolationDetailCountRowMapper;
import org.egov.ec.repository.rowmapper.ViolationDetailRowMapper;
import org.egov.ec.repository.rowmapper.ViolationDetailRowMapperV2;
import org.egov.ec.web.models.ChallanDataBckUp;
import org.egov.ec.web.models.EcPayment;
import org.egov.ec.web.models.EcPaymentData;
import org.egov.ec.web.models.EcSearchCriteria;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.Violation;
import org.egov.ec.web.models.ViolationCount;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ViolationRepository {

	private JdbcTemplate jdbcTemplate;

	private ViolationDetailRowMapper rowMapper;

	private ViolationDetailRowMapperV2 violationDetailRowMapperV2;

	private ViolationDetailCountRowMapper rowCountMapper;

	private ColumnsRowMapper columnsRowMapper;

	private Producer producer;

	private EchallanConfiguration config;

	private EcQueryBuilder ecQueryBuilder;

	private ReceiptNoRowMapper receiptNoRowMapper;

	public ViolationRepository(JdbcTemplate jdbcTemplate, Producer producer, EchallanConfiguration config,
			ViolationDetailRowMapper rowMapper, ColumnsRowMapper columnsRowMapper,
			ViolationDetailCountRowMapper rowCountMapper, ReceiptNoRowMapper receiptNoRowMapper,
			EcQueryBuilder ecQueryBuilder, ViolationDetailRowMapperV2 violationDetailRowMapperV2) {
		this.jdbcTemplate = jdbcTemplate;
		this.rowMapper = rowMapper;
		this.producer = producer;
		this.config = config;
		this.ecQueryBuilder = ecQueryBuilder;
		this.columnsRowMapper = columnsRowMapper;
		this.rowCountMapper = rowCountMapper;
		this.receiptNoRowMapper = receiptNoRowMapper;
		this.violationDetailRowMapperV2 = violationDetailRowMapperV2;
	}

	/**
	 * Pushes the request in generateChallan topic to save challan data
	 *
	 * @param violationMaster Violation model
	 */
	public void generateChallan(@Valid Violation violationMaster) {
		log.info("Violation Repository - generateChallan Method");
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(violationMaster).build();
		producer.push(config.getGenerateChallanTopic(), infoWrapper);

	}

	/**
	 * Pushes the request in updateChallan topic to update challan status
	 *
	 * @param violationMaster Violation model
	 */
	public void updateChallan(@Valid Violation violationMaster) {
		log.info("Violation Repository - updateChallan Method");
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(violationMaster).build();
		producer.push(config.getUpdateChallanTopic(), infoWrapper);
	}

	/**
	 * fetches the list of challans for different screens
	 *
	 * @param searchCriteria Search criteria to apply filter
	 * @return Returns the list of challans
	 */

	public List<Violation> getChallan(EcSearchCriteria searchCriteria) {
		log.info("Violation Repository - getChallan Method");

		List<Violation> violationDetailList = null;

		String parameter = "%" + searchCriteria.getSearchText() + "%";

		if (null != searchCriteria.getSearchText() && !searchCriteria.getSearchText().isEmpty()) {
			try {
				violationDetailList = jdbcTemplate.query(
						EcQueryBuilder.GET_VIOLATION_MASTER_SEARCH, new Object[] { parameter, parameter, parameter,
								parameter, parameter, parameter, parameter, parameter, searchCriteria.getTenantId() },
						rowMapper);
			} catch (Exception e) {
				log.error("Violation Service - Get Violation Exception" + e.getMessage());
			}

			if (violationDetailList.size() >= searchCriteria.getLimit()) {

				return new ArrayList<Violation>(violationDetailList.subList(0, searchCriteria.getLimit()));
			} else {
				return violationDetailList;
			}

		} else if (searchCriteria.getAction().equalsIgnoreCase("auctionChallan")) {
			System.out.println("Inside auctionChallan type action");
			violationDetailList = jdbcTemplate.query(EcQueryBuilder.GET_VIOLATION_MASTER_AUTION,
					new Object[] { searchCriteria.getTenantId(), searchCriteria.getFromDate(), searchCriteria.getToDate() }, rowMapper);
			return violationDetailList;

		} else if (searchCriteria.getAction().equalsIgnoreCase("ChallanSM")) {

			violationDetailList = jdbcTemplate.query(EcQueryBuilder.GET_VIOLATION_MASTER_SM,
					new Object[] { searchCriteria.getTenantId() }, rowMapper);
			return violationDetailList;

		} else {
			violationDetailList = jdbcTemplate.query(EcQueryBuilder.GET_VIOLATION_MASTER,
					new Object[] { searchCriteria.getTenantId() }, rowMapper);

			if (violationDetailList.size() >= searchCriteria.getLimit()) {
				return new ArrayList<Violation>(violationDetailList.subList(0, searchCriteria.getLimit()));

			} else {
				return violationDetailList;
			}
		}
	}

	/**
	 * fetches the penalty amount and fine date validation
	 *
	 * @param violationMaster Violation model
	 * @return Returns the penalty amount
	 */
	public String getpenalty(@Valid Violation violationMaster) {
		log.info("Violation Repository - getpenalty Method");
		String numberOfViolation;
		String x = null;
		try {
			if (violationMaster.getEncroachmentType().equals("Seizure of Vehicles")) {
				numberOfViolation = violationMaster.getViolationItem().get(0).getItemType();
			} else {
				numberOfViolation = violationMaster.getNumberOfViolation() + "";
			}
			x = jdbcTemplate.queryForObject(EcQueryBuilder.GET_FINE_PENALTY_AMOUNT,
					new Object[] { violationMaster.getEncroachmentType(), numberOfViolation },

					(String.class));
		} catch (Exception e) {
			return x;
		}
		return x;

	}

	/**
	 * Pushes the request in AddpaymentHistory topic to to maintain payment history
	 * for online payment
	 *
	 * @param ecPayment EcPayment model
	 */
	public void addPayment(EcPayment ecPayment) {
		log.info("Violation Repository - addPayment Method");
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(ecPayment).build();
		producer.push(config.getAddpaymentHistoryTopic(), infoWrapper);

	}

	/**
	 * fetches the list of challans which are marked as defective by storemanager
	 *
	 * @param searchCriteria EcSearchCriteria model
	 * @return Returns the list of challans
	 */
	public List<Violation> getChallanForHOD(EcSearchCriteria searchCriteria) {
		log.info("Violation Repository - getChallanForHOD Method");

		List<Violation> violationDetailList = jdbcTemplate.query(EcQueryBuilder.GET_VIOLATION_MASTER_HOD,
				new Object[] { searchCriteria.getTenantId() }, rowMapper);
		return violationDetailList;
	}

	/**
	 * fetches the list of challans whoose auctions are need to be approved by hod
	 *
	 * @param searchCriteria EcSearchCriteria model
	 * @return Returns the list of challans
	 */
	public List<Violation> getChallanForAuctionHOD(EcSearchCriteria searchCriteria) {
		log.info("Violation Repository - getChallanForAuctionHOD Method");

		List<Violation> violationDetailList = jdbcTemplate.query(EcQueryBuilder.GET_VIOLATION_MASTER_AUCTION_HOD,
				new Object[] { searchCriteria.getTenantId() }, rowMapper);
		return violationDetailList;
	}

	/**
	 * fetches the list of challans on the basis of search criterias
	 *
	 * @param searchCriteria EcSearchCriteria model
	 * @return Returns the list of challans
	 */
//	public List<Violation> getSearchChallan(Violation violation) {
//		log.info("Violation Repository - getSearchChallan Method");
//
//		List<Violation> violationDetailList;
//
//		violationDetailList = jdbcTemplate.query(EcQueryBuilder.SEARCH_VIOLATION_MASTER,
//				new Object[] { violation.getFromDate(), violation.getFromDate(), violation.getToDate(),
//						violation.getToDate(), violation.getSiName(), violation.getSiName(),
//						violation.getEncroachmentType(), violation.getEncroachmentType(), violation.getSector(),
//						violation.getSector(), violation.getStatus(), violation.getStatus(), violation.getTenantId(),
//						violation.getChallanId(), violation.getChallanId() },
//				rowMapper);
//
//		return violationDetailList;
//
//	}

	public List<Violation> getSearchChallan(Violation violation) {
		log.info("Violation Repository - getSearchChallan Method");

		List<Violation> violationDetailList;

		violationDetailList = jdbcTemplate.query(EcQueryBuilder.SEARCH_VIOLATION_MASTER,
				new Object[] { violation.getStatus(), violation.getStatus(), violation.getFromDate(),
						violation.getFromDate(), violation.getToDate(), violation.getToDate(),
						violation.getEncroachmentType(), violation.getEncroachmentType(), violation.getSiName(),
						violation.getSiName(), violation.getSector(), violation.getSector(),
						violation.getLicenseNoCov(), violation.getLicenseNoCov(),
						violation.getChallanId(), violation.getChallanId() },
				violationDetailRowMapperV2);

		return violationDetailList;

	}

	/**
	 * fetches the list of challans on the basis of search criterias For Count
	 *
	 * @param searchCriteria EcSearchCriteria model
	 * @return Returns the list of challans
	 */
	public List<ViolationCount> getSearchChallanCount(ViolationCount violation) {
		log.info("Violation Repository - getSearchChallan Method");

		List<ViolationCount> violationDetailList;

		violationDetailList = jdbcTemplate.query(EcQueryBuilder.SEARCH_VIOLATION_MASTER_COUNT, new Object[] {
				violation.getFromDate(), violation.getFromDate(), violation.getToDate(), violation.getToDate() },
				rowCountMapper);

		return violationDetailList;

	}

	public void updatePayment(EcPaymentData ecPayment) {
		log.info("Violation Repository - updatePayment Method");
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(ecPayment).build();
		producer.push(config.getUpdatePayment(), infoWrapper);
	}

	public void deleteChallan(ChallanDataBckUp bck) {
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(bck).build();
		producer.push(config.getDataBckChallan(), infoWrapper);

	}

	public JSONArray getdataProcessInstance(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_PROCESS_INSTANCE,
				new Object[] { bck.getChallanId(), bck.getChallanId() }, columnsRowMapper);
	}

	public JSONArray getdataEgecDocument(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_DOCUMENt, new Object[] { bck.getChallanId() },
				columnsRowMapper);
	}

	public JSONArray getdataStoreItem(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_STORE_ITEM, new Object[] { bck.getChallanId() },
				columnsRowMapper);
	}

	public JSONArray getdatPayment(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_PAYMENT, new Object[] { bck.getChallanId() }, columnsRowMapper);
	}

	public JSONArray getdatChallanDetail(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_CHALLAN_DETAILS, new Object[] { bck.getChallanId() },
				columnsRowMapper);
	}

	public JSONArray getdatChallanMaster(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_CHALLAN_MASTER, new Object[] { bck.getChallanId() },
				columnsRowMapper);
	}

	public JSONArray getdatViolationDetail(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_VIOLATION_DETAIL, new Object[] {}, columnsRowMapper);
	}

	public JSONArray getdatViolationMaster(ChallanDataBckUp bck) {
		return jdbcTemplate.query(EcQueryBuilder.SEARCH_VIOLATION_MASTER_DETAILS, new Object[] {}, columnsRowMapper);
	}

	public void editChallan(Violation violationMaster) {
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(violationMaster).build();
		producer.push(config.getEditChallanTopic(), infoWrapper);
	}

	public String getReceiptNo(EcSearchCriteria searchCriteria) {
		String receiptno = "";

		try {
			return receiptno = jdbcTemplate.query(EcQueryBuilder.GET_RECEIPT_NO,
					new Object[] { searchCriteria.getSearchText() }, receiptNoRowMapper);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("Exception", e.getMessage());
		}

	}
}
