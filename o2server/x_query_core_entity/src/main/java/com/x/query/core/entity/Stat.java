package com.x.query.core.entity;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.jdbc.ContainerTable;
import org.apache.openjpa.persistence.jdbc.ElementColumn;
import org.apache.openjpa.persistence.jdbc.ElementIndex;
import org.apache.openjpa.persistence.jdbc.Index;

import com.x.base.core.entity.AbstractPersistenceProperties;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.SliceJpaObject;
import com.x.base.core.entity.annotation.CheckPersist;
import com.x.base.core.entity.annotation.CitationExist;
import com.x.base.core.entity.annotation.CitationNotExist;
import com.x.base.core.entity.annotation.ContainerEntity;
import com.x.base.core.entity.annotation.Equal;
import com.x.base.core.entity.annotation.Flag;
import com.x.base.core.project.annotation.FieldDescribe;

@Entity
@ContainerEntity(dumpSize = 10, type = ContainerEntity.Type.content, reference = ContainerEntity.Reference.strong)
@Table(name = PersistenceProperties.Stat.table, uniqueConstraints = {
		@UniqueConstraint(name = PersistenceProperties.Stat.table + JpaObject.IndexNameMiddle
				+ JpaObject.DefaultUniqueConstraintSuffix, columnNames = { JpaObject.IDCOLUMN,
						JpaObject.CREATETIMECOLUMN, JpaObject.UPDATETIMECOLUMN, JpaObject.SEQUENCECOLUMN }) })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Stat extends SliceJpaObject {

	private static final long serialVersionUID = -7378861736912696466L;

	private static final String TABLE = PersistenceProperties.Stat.table;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@FieldDescribe("???????????????,????????????.")
	@Id
	@Column(length = length_id, name = ColumnNamePrefix + id_FIELDNAME)
	private String id = createId();

	/* ????????? JpaObject ???????????? */

	public void onPersist() throws Exception {

	}

	/* ?????????????????? */

	/* Entity ?????????????????? */

	public static final String name_FIELDNAME = "name";
	@Flag
	@FieldDescribe("??????.")
	@Column(length = length_255B, name = ColumnNamePrefix + name_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + name_FIELDNAME)
	@CheckPersist(allowEmpty = false, simplyString = false, citationNotExists =
	/* ?????????????????? */
	@CitationNotExist(fields = { "name", "id",
			"alias" }, type = Stat.class, equals = @Equal(field = "query", property = "query")))
	private String name;

	public static final String alias_FIELDNAME = "alias";
	@Flag
	@FieldDescribe("??????.")
	@Column(length = length_255B, name = ColumnNamePrefix + alias_FIELDNAME)
	@CheckPersist(allowEmpty = true, simplyString = false, citationNotExists =
	/* ?????????????????? */
	@CitationNotExist(fields = { "name", "id",
			"alias" }, type = Stat.class, equals = @Equal(field = "query", property = "query")))
	private String alias;

	public static final String description_FIELDNAME = "description";
	@FieldDescribe("??????.")
	@Column(length = length_255B, name = ColumnNamePrefix + description_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String description;

	public static final String query_FIELDNAME = "query";
	@FieldDescribe("????????????.")
	@Column(length = JpaObject.length_id, name = ColumnNamePrefix + query_FIELDNAME)
	@Index(name = TABLE + ColumnNamePrefix + query_FIELDNAME)
	@CheckPersist(allowEmpty = false, citationExists = { @CitationExist(type = Query.class) })
	private String query;

	public static final String view_FIELDNAME = "view";
	@FieldDescribe("???????????????.")
	@Column(length = length_id, name = ColumnNamePrefix + view_FIELDNAME)
	@CheckPersist(allowEmpty = true, citationExists = { @CitationExist(type = View.class) })
	private String view;

	public static final String data_FIELDNAME = "data";
	@FieldDescribe("????????????.")
	@Lob
	@Basic(fetch = FetchType.EAGER)
	@Column(length = JpaObject.length_10M, name = ColumnNamePrefix + data_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String data;

	public static final String availableIdentityList_FIELDNAME = "availableIdentityList";
	@FieldDescribe("?????????????????????.")
	@PersistentCollection(fetch = FetchType.EAGER)
	@ContainerTable(name = TABLE + ContainerTableNameMiddle
			+ availableIdentityList_FIELDNAME, joinIndex = @Index(name = TABLE + IndexNameMiddle
					+ availableIdentityList_FIELDNAME + JoinIndexNameSuffix))
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ElementColumn(length = length_255B, name = ColumnNamePrefix + availableIdentityList_FIELDNAME)
	@ElementIndex(name = TABLE + IndexNameMiddle + availableIdentityList_FIELDNAME + ElementIndexNameSuffix)
	@CheckPersist(allowEmpty = true)
	private List<String> availableIdentityList;

	public static final String availableUnitList_FIELDNAME = "availableUnitList";
	@FieldDescribe("?????????????????????.")
	@PersistentCollection(fetch = FetchType.EAGER)
	@ContainerTable(name = TABLE + ContainerTableNameMiddle
			+ availableUnitList_FIELDNAME, joinIndex = @Index(name = TABLE + IndexNameMiddle
					+ availableUnitList_FIELDNAME + JoinIndexNameSuffix))
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ElementColumn(length = length_255B, name = ColumnNamePrefix + availableUnitList_FIELDNAME)
	@ElementIndex(name = TABLE + IndexNameMiddle + availableUnitList_FIELDNAME + ElementIndexNameSuffix)
	@CheckPersist(allowEmpty = true)
	private List<String> availableUnitList;

	public static final String display_FIELDNAME = "display";
	@FieldDescribe("??????????????????.")
	@Column(name = ColumnNamePrefix + display_FIELDNAME)
	private Boolean display;

	public static final String orderNumber_FIELDNAME = "orderNumber";
	@FieldDescribe("?????????,???????????????")
	@Column(name = ColumnNamePrefix + orderNumber_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + orderNumber_FIELDNAME)
	private Integer orderNumber;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public List<String> getAvailableIdentityList() {
		return availableIdentityList;
	}

	public void setAvailableIdentityList(List<String> availableIdentityList) {
		this.availableIdentityList = availableIdentityList;
	}

	public List<String> getAvailableUnitList() {
		return availableUnitList;
	}

	public void setAvailableUnitList(List<String> availableUnitList) {
		this.availableUnitList = availableUnitList;
	}

	public Boolean getDisplay() {
		return display;
	}

	public void setDisplay(Boolean display) {
		this.display = display;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}

}