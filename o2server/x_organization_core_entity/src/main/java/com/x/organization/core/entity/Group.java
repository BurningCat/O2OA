package com.x.organization.core.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.jdbc.ContainerTable;
import org.apache.openjpa.persistence.jdbc.ElementColumn;
import org.apache.openjpa.persistence.jdbc.ElementIndex;
import org.apache.openjpa.persistence.jdbc.Index;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.x.base.core.entity.AbstractPersistenceProperties;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.SliceJpaObject;
import com.x.base.core.entity.annotation.CheckPersist;
import com.x.base.core.entity.annotation.CheckRemove;
import com.x.base.core.entity.annotation.CitationExist;
import com.x.base.core.entity.annotation.CitationNotExist;
import com.x.base.core.entity.annotation.ContainerEntity;
import com.x.base.core.entity.annotation.Flag;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.tools.DateTools;

@Entity
@ContainerEntity(dumpSize = 200, type = ContainerEntity.Type.content, reference = ContainerEntity.Reference.strong)
@Table(name = PersistenceProperties.Group.table, uniqueConstraints = {
		@UniqueConstraint(name = PersistenceProperties.Group.table + JpaObject.IndexNameMiddle
				+ JpaObject.DefaultUniqueConstraintSuffix, columnNames = { JpaObject.IDCOLUMN,
						JpaObject.CREATETIMECOLUMN, JpaObject.UPDATETIMECOLUMN, JpaObject.SEQUENCECOLUMN }) })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Group extends SliceJpaObject {

	private static final long serialVersionUID = -7688990884958313153L;

	private static final String TABLE = PersistenceProperties.Group.table;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@FieldDescribe("???????????????,????????????.")
	@Id
	@Column(length = length_id, name = ColumnNamePrefix + id_FIELDNAME)
	@CheckRemove(citationNotExists = {
			/* ????????????????????????????????? */
			@CitationNotExist(type = Group.class, fields = Group.groupList_FIELDNAME),
			/* ????????????????????????????????? */
			@CitationNotExist(type = Role.class, fields = Role.groupList_FIELDNAME) })
	private String id = createId();

	/* ????????? JpaObject ???????????? */

	public void onPersist() throws Exception {
		this.pinyin = StringUtils.lowerCase(PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE));
		this.pinyinInitial = StringUtils.lowerCase(PinyinHelper.getShortPinyin(name));
		this.unique = StringUtils.isBlank(this.unique) ? id : unique;
		this.distinguishedName = this.name + PersistenceProperties.distinguishNameSplit + this.unique
				+ PersistenceProperties.distinguishNameSplit + PersistenceProperties.Group.distinguishNameCharacter;
		/** ????????????????????? */
		if (null == this.orderNumber) {
			this.orderNumber = DateTools.timeOrderNumber();
		}
	}

	/* ?????????????????? */

	/** ?????????????????? */

	public static final String pinyin_FIELDNAME = "pinyin";
	@FieldDescribe("name??????,????????????")
	@Index(name = TABLE + IndexNameMiddle + pinyin_FIELDNAME)
	@Column(length = length_255B, name = ColumnNamePrefix + pinyin_FIELDNAME)
	private String pinyin;

	public static final String pinyinInitial_FIELDNAME = "pinyinInitial";
	@FieldDescribe("name???????????????,????????????")
	@Column(length = AbstractPersistenceProperties.organization_name_length, name = ColumnNamePrefix
			+ pinyinInitial_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + pinyinInitial_FIELDNAME)
	private String pinyinInitial;

	public static final String description_FIELDNAME = "description";
	@FieldDescribe("??????.")
	@Column(length = length_255B, name = ColumnNamePrefix + description_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + description_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String description;

	public static final String name_FIELDNAME = "name";
	@Flag
	@FieldDescribe("????????????.?????????")
	@Column(length = length_255B, name = ColumnNamePrefix + name_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + name_FIELDNAME)
	@CheckPersist(allowEmpty = false, simplyString = true)
	private String name;

	public static final String unique_FIELDNAME = "unique";
	@Flag
	@FieldDescribe("????????????,????????????,??????????????????????????????")
	@Column(length = length_255B, name = ColumnNamePrefix + unique_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + unique_FIELDNAME)
	@CheckPersist(allowEmpty = true, simplyString = true, citationNotExists = @CitationNotExist(fields = unique_FIELDNAME, type = Group.class))
	private String unique;

	public static final String distinguishedName_FIELDNAME = "distinguishedName";
	@Flag
	@FieldDescribe("?????????.")
	@Column(length = length_255B, name = ColumnNamePrefix + distinguishedName_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + distinguishedName_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String distinguishedName;

	public static final String orderNumber_FIELDNAME = "orderNumber";
	@FieldDescribe("?????????,????????????,???????????????")
	@Column(name = ColumnNamePrefix + orderNumber_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + orderNumber_FIELDNAME)
	private Integer orderNumber;

	public static final String personList_FIELDNAME = "personList";
	@FieldDescribe("?????????????????????.???????????? ID.")
	@PersistentCollection(fetch = FetchType.EAGER)
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ContainerTable(name = TABLE + ContainerTableNameMiddle + personList_FIELDNAME, joinIndex = @Index(name = TABLE
			+ IndexNameMiddle + personList_FIELDNAME + JoinIndexNameSuffix))
	@ElementColumn(length = JpaObject.length_id, name = ColumnNamePrefix + personList_FIELDNAME)
	@ElementIndex(name = TABLE + IndexNameMiddle + personList_FIELDNAME + ElementIndexNameSuffix)
	@CheckPersist(allowEmpty = true, citationExists = @CitationExist(type = Person.class))
	private List<String> personList;

	public static final String groupList_FIELDNAME = "groupList";
	@FieldDescribe("?????????????????????.???????????? ID.")
	@ContainerTable(name = TABLE + ContainerTableNameMiddle + groupList_FIELDNAME, joinIndex = @Index(name = TABLE
			+ IndexNameMiddle + groupList_FIELDNAME + JoinIndexNameSuffix))
	@ElementIndex(name = TABLE + IndexNameMiddle + groupList_FIELDNAME + ElementIndexNameSuffix)
	@PersistentCollection(fetch = FetchType.EAGER)
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ElementColumn(length = JpaObject.length_id, name = ColumnNamePrefix + groupList_FIELDNAME)
	@CheckPersist(allowEmpty = true, citationExists = @CitationExist(type = Group.class))
	private List<String> groupList;

	public static final String unitList_FIELDNAME = "unitList";
	@FieldDescribe("????????????????????????,??????unit ID.")
	@ContainerTable(name = TABLE + ContainerTableNameMiddle + unitList_FIELDNAME, joinIndex = @Index(name = TABLE
			+ IndexNameMiddle + unitList_FIELDNAME + JoinIndexNameSuffix))
	@ElementIndex(name = TABLE + IndexNameMiddle + unitList_FIELDNAME + ElementIndexNameSuffix)
	@PersistentCollection(fetch = FetchType.EAGER)
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ElementColumn(length = JpaObject.length_id, name = ColumnNamePrefix + unitList_FIELDNAME)
	@CheckPersist(allowEmpty = true, citationExists = @CitationExist(type = Unit.class))
	private List<String> unitList;

	public static final String identityList_FIELDNAME = "identityList";
	@FieldDescribe("????????????????????????,identity ID.")
	@ContainerTable(name = TABLE + ContainerTableNameMiddle + identityList_FIELDNAME, joinIndex = @Index(name = TABLE
			+ IndexNameMiddle + identityList_FIELDNAME + JoinIndexNameSuffix))
	@ElementIndex(name = TABLE + IndexNameMiddle + identityList_FIELDNAME + ElementIndexNameSuffix)
	@PersistentCollection(fetch = FetchType.EAGER)
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ElementColumn(length = JpaObject.length_id, name = ColumnNamePrefix + identityList_FIELDNAME)
	@CheckPersist(allowEmpty = true, citationExists = @CitationExist(type = Identity.class))
	private List<String> identityList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnique() {
		return unique;
	}

	public void setUnique(String unique) {
		this.unique = unique;
	}

	public List<String> getPersonList() {
		return personList;
	}

	public void setPersonList(List<String> personList) {
		this.personList = personList;
	}

	public List<String> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<String> groupList) {
		this.groupList = groupList;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getPinyinInitial() {
		return pinyinInitial;
	}

	public void setPinyinInitial(String pinyinInitial) {
		this.pinyinInitial = pinyinInitial;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getUnitList() {
		return unitList;
	}

	public void setUnitList(List<String> unitList) {
		this.unitList = unitList;
	}

	public List<String> getIdentityList() {
		return identityList;
	}

	public void setIdentityList(List<String> identityList) {
		this.identityList = identityList;
	}
}
