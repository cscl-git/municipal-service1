package org.egov.ec.repository.builder;

import org.springframework.stereotype.Component;

@Component
public class EcQueryBuilder {

	public static final String GET_ITEM_MASTER = "select * from egec_item_master where tenant_id=? and is_active = 'TRUE'::boolean order by last_modified_time desc";
	public static final String GET_ITEM_MASTER_PENDING = "select * from egec_item_master where approval_status='PENDING' and tenant_id=?";
	public static final String GET_FINE_MASTER = "select * from egec_fine_master where tenant_id=? and is_active = 'TRUE'::boolean order by last_modified_time desc";
	public static final String GET_FINE_MASTER_PENDING = "select * from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean";
	public static final String GET_VIOLATION_MASTER = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end )as challan_status,*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount, \n"
			+ "(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
			+ "  from egec_violation_master violation\n"
			+ "	JOIN egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ "	JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
			+ "	JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid\n"
			+ "	LEFT JOIN egec_document doc on violation.violation_uuid = doc.violation_uuid\n"
			+ "	where violation.tenant_id=?  order by violation.last_modified_time desc";

	public static final String GET_VIOLATION_MASTER_AUTION = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+ "*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,\n"
			+ "(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount\n"
			+ " from egec_violation_master violation \n"
			+ " JOIN egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ " JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
			+ " JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			+ " JOIN egec_store_item_register storeItem on violation.violation_uuid = storeItem.violation_uuid \n"
			+ " LEFT JOIN egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			+ " where violation.tenant_id=? and challan.challan_status='PENDING FOR AUCTION' and violation.created_time >=? \n"
			+ "	and violation.created_time <=? order by violation.last_modified_time desc";

	public static final String GET_VIOLATION_MASTER_SM = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+ "	*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,\n"
			+ "	(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount\n"
			+ " from egec_violation_master violation \n"
			+ " JOIN egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ " JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
			+ " JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			+ " LEFT JOIN egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			+ " where violation.tenant_id='ch.chandigarh' and challan.challan_status not in  ('CHALLAN ISSUED', 'CLOSED') order by violation.last_modified_time desc";

	public static final String GET_FINE_PENALTY_AMOUNT = "select penalty_amount from egec_fine_master where encroachment_type = ?::varchar and number_of_violation=?::varchar  and is_active = 'TRUE'::boolean and now()::date BETWEEN effective_start_date \n"
			+ "                 AND effective_end_date::date and approval_status = 'APPROVED'";
	/*
	 * public static final String GET_VIOLATION_MASTER_SEARCH =
	 * "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
	 * +
	 * "			payment.last_modified_time as last_modified_time,*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,\n"
	 * +
	 * "			(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
	 * + "			 from egec_violation_master violation \n" +
	 * "			join egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
	 * +
	 * "			join egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
	 * +
	 * "    		left join egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
	 * +
	 * "    		left join egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
	 * +
	 * "    		where violation.contact_number ilike ? or challan.challan_id ilike ? or challan.challan_uuid ilike ? \n"
	 * +
	 * "    		or violation.violator_name ilike ? or violation.license_no_cov ilike ? or violation.si_name ilike ? \n"
	 * +
	 * "    		or violation.sector ilike ? or violation.encroachment_type ilike ? and violation.tenant_id=? order by violation.last_modified_time desc"
	 * ;
	 */
	
	public static final String GET_VIOLATION_MASTER_SEARCH = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+ "			payment.last_modified_time as last_modified_time,*,fine.head_amount as fineAmount,\n"
			+ "			pen.head_amount as penaltyAmount \n"
			+ "			from egec_violation_master violation \n"
			+ "			join egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
			+ "			join egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			+ "    		left join egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ "    		left join egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			+ "			left join egec_challan_detail fine on fine.challan_uuid = challan.challan_uuid and fine.budget_head = 'FINE_AMOUNT' \n"
			+ " 		left join egec_challan_detail pen on pen.challan_uuid = challan.challan_uuid and pen.budget_head = 'PENALTY_AMOUNT' \n"
			+ "    		where violation.contact_number ilike ? or challan.challan_id ilike ? or challan.challan_uuid ilike ? \n"
			+ "    		or violation.violator_name ilike ? or violation.license_no_cov ilike ? or violation.si_name ilike ? \n"
			+ "    		or violation.sector ilike ? or violation.encroachment_type ilike ? and violation.tenant_id=? order by violation.last_modified_time desc";


	public static final String EG_PF_TRANSACTION_DATA = "select txn_id from eg_pg_transactions ep where ep.consumer_code =?";
	public static final String GET_PENALTY_VIOLATIONS = "select vm.violation_uuid as violationUuid,cm.challan_Uuid as challanUuid,((current_date - vs.item_store_deposit_date)-6) * coalesce(fm.storage_charges,0) as storageCharges from egec_store_item_register  vs inner join egec_violation_master vm on vm.violation_uuid = vs.violation_uuid inner join egec_violation_detail vd on vm.violation_uuid = vd.violation_uuid inner join egec_fine_master fm on vd.item_type = fm.number_of_violation  inner join egec_payment ep on ep.violation_uuid = vm.violation_uuid and vd.violation_uuid = ep.violation_uuid  inner join egec_challan_master cm on cm.violation_uuid = vm.violation_uuid where ((current_date - vs.item_store_deposit_date)-6) > 0 and vm.violation_date < now()- interval '7 days' and vm.encroachment_type = 'Seizure of Vehicles' and ep.payment_status = 'PENDING'  and cm.challan_status !='CLOSED' and now()::date between fm.effective_start_date and fm.effective_end_date and vm.tenant_id = ?";

	public static final String GET_STORE_ITEM_REGISTER = "select * from egec_store_item_register item \n" +
	// "join egec_document doc on doc.violation_uuid = item.violation_uuid\n"
	// +
			"where item.tenant_id=?";
	public static final String GET_STORE_ITEM_REGISTER_SEARCH = "select * from egec_store_item_register item\n" +
	// "join egec_document doc on doc.violation_uuid = item.violation_uuid\n"
	// +
			" where item.challan_uuid like ? or item.item_name like ? and item.tenant_id=?";

	public static final String GET_VENDOR_DETAIL_COV = "select *,(select case when count(*)<5 then (count(*)+1) else 5 end from egec_violation_master v where v.license_no_cov=egec_vendor_registration_master.cov_no  ) as numberOfViolation from egec_vendor_registration_master where egec_vendor_registration_master.cov_no in(:covNo) order by last_modified_time desc ";
	public static final String GET_VENDOR_DETAIL = "select *,(select case when count(*)<5 then (count(*)+1) else 5 end from egec_violation_master v where v.license_no_cov=egec_vendor_registration_master.cov_no  ) as numberOfViolation from egec_vendor_registration_master order by last_modified_time desc ";
	public static final String GET_VENDOR_DETAIL_SEARCH = "select distinct on (cov_no) *,(select case when count(*)<5 then (count(*)+1) else 5 end from egec_violation_master v where v.license_no_cov=egec_vendor_registration_master.cov_no  ) as numberOfViolation from egec_vendor_registration_master \n"
			+ "where cov_no like ? or contact_number ilike ? or name ilike ?";

	public static final String GET_PAYMENT_REPORT = "select *,(select case when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and master.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status from egec_violation_master master, egec_challan_master challan, egec_payment payment where master.violation_uuid=challan.violation_uuid and challan.violation_uuid=payment.violation_uuid and master.violation_date between ?  and  ? and (? = '' or payment_status= ?) and master.tenant_id=? order by master.created_time";
	public static final String GET_SEIZURE_REPORT = "select *,(select case when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and master.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status \n"
			+ " from egec_violation_master master, egec_challan_master challan, egec_payment payment where master.violation_uuid=challan.violation_uuid and challan.violation_uuid=payment.violation_uuid and master.violation_date between ?  and  ? and (?  ilike '' or master.si_name ilike ?)  and(? ilike '' or master.encroachment_type ilike ?) and (? ilike '' or master.sector ilike ?) \n"
			+ " and(? ilike '' or ((select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )) ilike ?) \n"
			+ " and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT1 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, challan.challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '0 days'  and item_store_deposit_date > now()- interval '11 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT2 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, challan.challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '11 days'  and item_store_deposit_date > now()- interval '21 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT3 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, challan.challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '21 days'  and item_store_deposit_date > now()- interval '31 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT4 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '31 days'  and item_store_deposit_date > now()- interval '10000 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT5 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";

	public static final String GET_AUCTION_MASTER = "select *,(select store.item_name from egec_store_item_register store where store.store_item_uuid=detail.store_item_uuid) from egec_auction_master auction join egec_auction_detail detail on auction.auction_uuid=detail.auction_uuid where auction.challan_uuid=? and auction.tenant_id=?";

	public static final String GET_STORE_AUCTION_ITEM = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and ((item_store_deposit_date < now()- interval '30 days' and master.encroachment_type <> 'Seizure of Vehicles') OR(item_store_deposit_date < now()- interval '365 days' and master.encroachment_type = 'Seizure of Vehicles'))  and item_store_deposit_date > now()- interval '10000 days'  and challan.challan_status <> 'CLOSED' and challan.tenant_id = ?";
	public static final String GET_STORE_ITEM_REGISTER_HOD = "select * from egec_store_item_register where challan_uuid in (\n"
			+ "	select challan_uuid from egec_store_item_register  where isverified in (false)) and tenant_id = ?";

	public static final String GET_VIOLATION_MASTER_HOD = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+ " *,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount, \n"
			+ "			(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
			+ "	from egec_violation_master violation \n"
			+ "	 JOIN egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ "	 JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
			+ "	 JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			+ "	 LEFT JOIN egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			+ "	 where challan.challan_uuid in (select challan_uuid from egec_store_item_register  where isverified in (false)) \n"
			+ "	 and violation.tenant_id=? and challan.challan_status <> 'CLOSED' order by violation.last_modified_time desc";

	public static final String GET_VIOLATION_MASTER_AUCTION_HOD = "select *,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,  \n"
			+ "    		(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
			+ " from egec_violation_master violation \n"
			+ " JOIN egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ "  JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid  \n"
			+ " JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			+ " JOIN egec_auction_master auction on violation.violation_uuid = auction.violation_uuid \n"
			+ " LEFT JOIN egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			+ " where auction.status='PENDING' and violation.tenant_id=? \n"
			+ "	order by violation.last_modified_time desc	";

	public static final String GET_AUCTION_CHALLAN_MASTER = "\n"
			+ "		select auction.*,violation.si_name,violation.violator_name,violation.encroachment_type,violation.sector,challan.challan_id,violation.violation_date,violation.contact_number from egec_auction_master auction \n"
			+ "		JOIN egec_violation_master violation ON auction.violation_uuid=violation.violation_uuid \n"
			+ "		JOIN egec_challan_master challan ON auction.violation_uuid=challan.violation_uuid\n"
			+ "		where auction.tenant_id=? and auction.status='PENDING' order by violation.last_modified_time desc";
    //public static final String GET_AUCTIONED_AVAILABLE_COUNT = "select b.challan_uuid,b.challan_id from (select ( (select count(*) from egec_store_item_register sr where sr.quantity::int != auctioned_quantity::int  and sr.challan_uuid = a.challan_uuid ) +(select count(*) from egec_auction_master am where am.status = 'PENDING' and am.challan_uuid = a.challan_uuid )) as quantityCount ,a.challan_uuid,a.challan_id from (select * from egec_challan_master where challan_status = 'PENDING FOR AUCTION' and tenant_id = ?) a group by a.challan_uuid,a.challan_uuid,a.challan_id) b where b.quantityCount = 0 ";

	public static final String GET_AUCTIONED_AVAILABLE_COUNT = "SELECT cm.challan_uuid,cm.challan_id FROM egec_challan_master cm WHERE cm.challan_status = 'PENDING FOR AUCTION' AND cm.tenant_id = ?  AND NOT EXISTS ( SELECT 1 FROM egec_store_item_register sr WHERE sr.challan_uuid = cm.challan_uuid AND sr.quantity <> sr.auctioned_quantity ) AND NOT EXISTS ( SELECT 1 FROM egec_auction_master am WHERE am.challan_uuid = cm.challan_uuid AND am.status = 'PENDING') AND EXISTS ( SELECT 1 FROM egec_payment p WHERE p.challan_uuid = cm.challan_uuid AND p.payment_status <> 'PENDING' )";
	public static final String GET_FINE_VALIDATION_DATE = "select count(*) as  from egec_fine_master where encroachment_type = ?::varchar and number_of_violation=?::varchar and is_active = 'TRUE'::boolean  and approval_status = 'APPROVED' and fine_Uuid != ? and ( effective_start_date BETWEEN ? AND ?::date  or  effective_end_date BETWEEN ? AND ?::date  )";
	public static final String GET_DASHBOARD_DETAILS_SI = "select count(distinct challan.challan_id) as challanCount from egec_violation_master violation, egec_violation_detail item, egec_document doc, egec_challan_master challan, egec_payment payment \n"
			+ " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid = doc.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?";
	public static final String GET_DASHBOARD_DETAILS_SM = "select (select count(distinct challan.challan_id) from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment \n"
			+ " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=? and challan.challan_status not in ('CLOSED' ,'CHALLAN ISSUED')) as challanCount,\n"
			+ "(select count(distinct challan.challan_id)\n"
			+ "			from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment,egec_store_item_register storeItem\n"
			+ "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n"
			+ "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n"
			+ "			and violation.violation_uuid = payment.violation_uuid\n"
			+ "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";

	public static final String GET_DASHBOARD_DETAILS_HOD = "select (select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n"
			+ " 			(select count(distinct auction.auction_uuid)  \n"
			+ "    		from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
			+ "        	where violation.violation_uuid = item.violation_uuid and\n"
			+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
			+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') as auctionCount, \n"
			+ "			(select count(distinct challan.challan_id)	\n"
			+ "			 from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment\n"
			+ "			where violation.violation_uuid = item.violation_uuid and\n"
			+ "			violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid \n"
			+ "			and challan.challan_uuid in (select challan_uuid from egec_store_item_register  where isverified in (false))\n"
			+ "			and violation.tenant_id=? and challan.challan_status <> 'CLOSED' ) as challanCount";

	public static final String GET_DASHBOARD_DETAILS_SI_SM = "select(select count(distinct challan.challan_id) as challanCount from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment \n"
			+ " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?) as challanCount, \n"
			+ "(select count(distinct challan.challan_id)\n"
			+ "			from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment,egec_store_item_register storeItem\n"
			+ "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n"
			+ "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n"
			+ "			and violation.violation_uuid = payment.violation_uuid\n"
			+ "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";

	public static final String GET_DASHBOARD_DETAILS_SI_HOD = "select(select count(distinct challan.challan_id) as challanCount from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment \n"
			+ " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?) as challanCount, \n"
			+ "(select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n"
			+ " 			(select count(distinct auction.auction_uuid)  \n"
			+ "    		from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
			+ "        	where violation.violation_uuid = item.violation_uuid and\n"
			+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
			+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') as auctionCount";

	public static final String GET_DASHBOARD_DETAILS_SM_HOD = "select (select count(distinct challan.challan_id) from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment \n"
			+ " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=? and challan.challan_status not in ('CLOSED' ,'CHALLAN ISSUED')) as challanCount,\n"
			+ "(select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n"
			+ " 			(select (select count(distinct auction.auction_uuid)  \n"
			+ "    		from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
			+ "        	where violation.violation_uuid = item.violation_uuid and\n"
			+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
			+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') + \n"
			+ "			(select count(distinct challan.challan_id)\n"
			+ "			from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment,egec_store_item_register storeItem\n"
			+ "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n"
			+ "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n"
			+ "			and violation.violation_uuid = payment.violation_uuid\n"
			+ "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";

	public static final String GET_DASHBOARD_DETAILS_SI_SM_HOD = "select (select count(distinct challan.challan_id) from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment \n"
			+ " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?) as challanCount,\n"
			+ "(select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n"
			+ " 			(select (select count(distinct auction.auction_uuid)  \n"
			+ "    		from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
			+ "        	where violation.violation_uuid = item.violation_uuid and\n"
			+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
			+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') + \n"
			+ "			(select count(distinct challan.challan_id)\n"
			+ "			from egec_violation_master violation, egec_violation_detail item, egec_challan_master challan, egec_payment payment,egec_store_item_register storeItem\n"
			+ "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n"
			+ "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n"
			+ "			and violation.violation_uuid = payment.violation_uuid\n"
			+ "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";

//	public static final String SEARCH_VIOLATION_MASTER = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,\r\n"
//			+ "(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount\r\n"
//			+ "  from egec_violation_master violation\r\n"
//			+ "JOIN egec_violation_detail item on violation.violation_uuid = item.violation_uuid \r\n"
//			+ "JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \r\n"
//			+ "JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid\r\n"
//			+ "LEFT JOIN egec_document doc on violation.violation_uuid = doc.violation_uuid			\r\n"
//			+ "where violation.violation_date >= CASE WHEN ?<>'' THEN DATE(?) ELSE 	violation.violation_date END \r\n"
//			+ " AND violation.violation_date <= CASE WHEN ?<>'' THEN DATE(?) ELSE violation.violation_date END and (?  ilike '' or violation.si_name ilike ?) \r\n"
//			+ " and(? ilike '' or violation.encroachment_type ilike ?) and (? ilike '' or violation.sector ilike ?)\r\n"
//			+ " and(? ilike '' or (select case when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION'  when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end) ilike ?)\r\n"
//			+ " and violation.tenant_id=? and UPPER(challan.challan_id) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(challan.challan_id) end,'%') order by violation.last_modified_time desc";

	public static final String SEARCH_VIOLATION_MASTER = "select 	max(case when store.item_store_deposit_date < now()- interval '30 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when challan.challan_status = 'CLOSED' and store.violation_uuid is not null then 'RELEASED FROM STORE' when challan.challan_status = 'CLOSED' and store.violation_uuid is null then 'RELEASED ON GROUND' else challan.challan_status end) as challan_status, max(violation.created_time) created_time, max(violation.last_modified_time) last_modified_time,	max(coalesce(ch.head_amount, 0)) as fineAmount, max(coalesce(chs.head_amount, 0)) as penaltyAmount,	max(violation.violation_uuid) as violation_uuid,max(contact_number) contact_number,	max(challan.challan_uuid) as challan_uuid,max(challan.tenant_Id) as tenant_Id,max(challan.challan_Id) as challan_Id,max(violation.encroachment_type) as encroachment_type,max(violation.violation_date) as violation_date,max(violation.violator_name) as violator_name,max(violation.sector) as sector,	max(violation.si_name) si_name,case when max(item.violation_item_uuid) is not null then json_agg(json_build_object('itemName', item.item_name, 'quantity', item.quantity)) else json_agg(json_build_object()) end violation_item, max(payment.payment_status) payment_status from egec_violation_master violation left join egec_challan_master challan on violation.violation_uuid = challan.violation_uuid left join egec_payment payment on 	violation.violation_uuid = payment.violation_uuid left join (select distinct violation_uuid,item_store_deposit_date from egec_store_item_register) store on store.violation_uuid = violation.violation_uuid left join egec_challan_detail ch on ch.budget_head = 'FINE_AMOUNT' and ch.challan_uuid = challan.challan_uuid left join  egec_challan_detail chs on chs.budget_head = 'PENALTY_AMOUNT' and chs.challan_uuid = challan.challan_uuid left join egec_violation_detail item on violation.violation_uuid = item.violation_uuid where case when store.item_store_deposit_date < now()- interval '30 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when challan.challan_status = 'CLOSED' and store.violation_uuid is not null then 'RELEASED FROM STORE' when challan.challan_status = 'CLOSED'	and  store.violation_uuid is null then 'RELEASED ON GROUND' else challan.challan_status end = case when ?<>'' then ? else case when store.item_store_deposit_date < now()- interval '30 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION' 		when challan.challan_status = 'CLOSED' and store.violation_uuid is not null then 'RELEASED FROM STORE' when challan.challan_status = 'CLOSED' and store.violation_uuid is null then 'RELEASED ON GROUND' else challan.challan_status end end and violation.violation_date >= case when ?<> '' then DATE(?) 		else violation.violation_date end and violation.violation_date <= case when ?<> '' then DATE(?) else violation.violation_date end and violation.encroachment_type = case when ?<>'' then ? else violation.encroachment_type end and violation.si_name = case when ?<>'' then ? else violation.si_name end and violation.sector = case when ?<>'' then ? else violation.sector end and violation.license_no_cov = case when ?<>'' then ? else violation.license_no_cov end and challan.challan_Id like concat(concat('%', case when ?<> '' then ? else challan.challan_Id end),'%') group by challan.challan_Id";

	/*
	 * public static final String SEARCH_VIOLATION_MASTER_COUNT = "select " // +
	 * "challan.challan_status  ," +
	 * "(select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,"
	 * + "* " + //
	 * "(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and "
	 * // + "ch.challan_uuid=challan.challan_uuid) as fineAmount,\r\n" + //
	 * "(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount\r\n"
	 * + "  from egec_violation_master violation\r\n" +
	 * "JOIN egec_violation_detail item on violation.violation_uuid = item.violation_uuid \r\n"
	 * +
	 * "JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \r\n"
	 * +
	 * "JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid\r\n"
	 * +
	 * "LEFT JOIN egec_document doc on violation.violation_uuid = doc.violation_uuid			\r\n"
	 * +
	 * "where violation.violation_date >= CASE WHEN ?<>'' THEN DATE(?) ELSE 	violation.violation_date END \r\n"
	 * +
	 * " AND violation.violation_date <= CASE WHEN ?<>'' THEN DATE(?) ELSE violation.violation_date END and (?  ilike '' or violation.si_name ilike ?) \r\n"
	 * +
	 * " and(? ilike '' or violation.encroachment_type ilike ?) and (? ilike '' or violation.sector ilike ?)\r\n"
	 * +
	 * " and(? ilike '' or (select case when (select store.item_store_deposit_date from egec_store_item_register store "
	 * +
	 * "where store.challan_uuid=challan.challan_uuid limit 1)< now()- interval '30 days' and challan.challan_status <> "
	 * +
	 * "'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION'  "
	 * +
	 * "when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store "
	 * +
	 * "where store.violation_uuid=violation.violation_uuid) > 0) then 'RELEASED FROM STORE' "
	 * +
	 * "when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store "
	 * +
	 * "where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end) "
	 * + "ilike ?)\r\n" + "  order by violation.last_modified_time desc";
	 */

	public static final String SEARCH_VIOLATION_MASTER_COUNT = "select violation.Violation_uuid,violation.encroachment_type, violation.violation_date, violation.sector, violation.si_name, violation.created_time,\r\n"
			+ "(case when challan.challan_status='CLOSED' and store.store_item_uuid is not null then 'RELEASED FROM STORE'\r\n"
			+ "when challan.challan_status='CLOSED' and store.store_item_uuid is null then 'RELEASED ON GROUND'\r\n"
			+ "else challan.challan_status end ) as challan_status, payment.payment_status,payment.payment_mode\r\n"
			+ "from egec_violation_master violation  \r\n"
			+ "JOIN egec_challan_master challan on violation.violation_uuid=challan.violation_uuid  \r\n"
			+ "JOIN egec_payment payment on violation.violation_uuid = payment.violation_uuid\r\n"
			+ "LEFT JOIN egec_store_item_register store on violation.violation_uuid = store.violation_uuid\r\n"
			+ "where violation.violation_date >= CASE WHEN ?<>'' THEN DATE(?) ELSE violation.violation_date END   \r\n"
			+ "AND violation.violation_date <= CASE WHEN ?<>'' THEN DATE(?) ELSE violation.violation_date end";

	public static final String GET_AUCTION_UUID_CHALLAN_MASTER = "\n"
			+ "		select auction.*,violation.si_name,violation.violator_name,violation.encroachment_type,violation.sector,challan.challan_id,violation.violation_date,violation.contact_number from egec_auction_master auction \n"
			+ "		JOIN egec_violation_master violation ON auction.violation_uuid=violation.violation_uuid \n"
			+ "		JOIN egec_challan_master challan ON auction.violation_uuid=challan.violation_uuid\n"
			+ "		where auction.auction_uuid=? and auction.tenant_id=? and auction.status='PENDING' order by violation.last_modified_time desc";

	// public static final String GET_CHALLAN_PENDING_AUCTION = "select challan.challan_uuid,challan.challan_id from egec_challan_master challan JOIN egec_violation_master violation on challan.violation_uuid=violation.violation_uuid JOIN egec_payment payment ON challan.challan_uuid=payment.challan_uuid\n"
	// 		+ "	where challan.tenant_id=? and challan.challan_uuid in \n"
	// 		+ "	(select store.challan_uuid from egec_store_item_register store where (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)\n"
	// 		+ "	< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles')\n"
	// 		+ "   and ((payment.payment_status = 'PENDING' and violation.encroachment_type <> 'Unauthorized/Unregistered Vendor') OR (violation.encroachment_type = 'Unauthorized/Unregistered Vendor')) and challan.challan_status <> 'CLOSED'";

	public static final String GET_CHALLAN_PENDING_AUCTION = "SELECT  c.challan_uuid,c.challan_id FROM egec_challan_master c JOIN egec_violation_master v  ON c.violation_uuid = v.violation_uuid "
			+ "JOIN egec_payment p ON c.challan_uuid = p.challan_uuid WHERE c.tenant_id = ? AND c.challan_status <> 'CLOSED' AND c.challan_status <> 'PENDING FOR AUCTION' AND c.challan_status='ADDED TO STORE' AND v.encroachment_type <> 'Seizure of Vehicles'"
			+ "  AND EXISTS (SELECT 1 FROM egec_store_item_register s WHERE s.challan_uuid = c.challan_uuid AND s.item_store_deposit_date < NOW() - INTERVAL '30 days' "
			+ "  ) AND ((p.payment_status = 'PENDING' AND v.encroachment_type <> 'Unauthorized/Unregistered Vendor')  OR (v.encroachment_type = 'Unauthorized/Unregistered Vendor'))";
			
	
	public static final String SEARCH_PROCESS_INSTANCE = "select * from eg_wf_processinstance_v2 ewpv where businessid in (select challan_uuid from egec_challan_master ecm where challan_id in ( ?)\r\n"
			+ "union select challan_id from egec_challan_master ecm where challan_id in (?))";
	public static final String SEARCH_DOCUMENt = "select * from egec_document where challan_uuid in (select challan_uuid from egec_challan_master ecm where challan_id in ( ?));";
	public static final String SEARCH_STORE_ITEM = "select * from egec_store_item_register where challan_uuid in (select challan_uuid from egec_challan_master ecm where challan_id in (?));";
	public static final String SEARCH_PAYMENT = "select * from egec_payment ep where ep.challan_uuid in (select challan_uuid from egec_challan_master ecm where challan_id in ( ?));";
	public static final String SEARCH_CHALLAN_DETAILS = "select *  from egec_challan_detail where challan_uuid in (select challan_uuid from egec_challan_master ecm where challan_id in (?));";
	public static final String SEARCH_CHALLAN_MASTER = "select * from egec_challan_master ecm where challan_id in ( ?);";
	public static final String SEARCH_VIOLATION_DETAIL = "SELECT * from egec_violation_detail evd where violation_uuid in (select violation_uuid from egec_violation_master evd\r\n"
			+ "where not exists (select 1 from egec_challan_master ecm where ecm.violation_uuid =evd.violation_uuid ));";
	public static final String SEARCH_VIOLATION_MASTER_DETAILS = "SELECT * from egec_violation_master evd\r\n"
			+ "where not exists (select 1 from egec_challan_master ecm where ecm.violation_uuid =evd.violation_uuid );";

	public static final String GET_RECEIPT_NO = " select distinct pyd.receiptnumber  from \r\n"
			+ "egec_challan_master ec   \r\n" + "INNER JOIN egcl_bill bill ON bill.consumercode  = ec.challan_id \r\n"
			+ "INNER JOIN egcl_paymentdetail pyd ON pyd.billid  = bill.id  \r\n"
			+ "INNER JOIN egcl_payment ep   ON ep.id = pyd.paymentid  \r\n"
			+ "where bill.consumercode  =? AND ec.tenant_id  = 'ch.chandigarh' ";
}
