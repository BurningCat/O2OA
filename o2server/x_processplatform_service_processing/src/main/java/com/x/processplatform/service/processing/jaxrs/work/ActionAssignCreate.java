package com.x.processplatform.service.processing.jaxrs.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.google.gson.JsonElement;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.project.annotation.ActionLogger;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.config.StorageMapping;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.gson.GsonPropertyObject;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WoId;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.organization.Unit;
import com.x.base.core.project.tools.ListTools;
import com.x.base.core.project.tools.StringTools;
import com.x.processplatform.core.entity.content.Attachment;
import com.x.processplatform.core.entity.content.Data;
import com.x.processplatform.core.entity.content.Work;
import com.x.processplatform.core.entity.content.WorkStatus;
import com.x.processplatform.core.entity.element.ActivityType;
import com.x.processplatform.core.entity.element.Application;
import com.x.processplatform.core.entity.element.Application_;
import com.x.processplatform.core.entity.element.Begin;
import com.x.processplatform.core.entity.element.Process;
import com.x.processplatform.core.entity.element.Process_;
import com.x.processplatform.core.express.ProcessingAttributes;
import com.x.processplatform.service.processing.Business;
import com.x.processplatform.service.processing.MessageFactory;
import com.x.processplatform.service.processing.Processing;
import com.x.processplatform.service.processing.ThisApplication;
import com.x.processplatform.service.processing.WorkDataHelper;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * ????????????start?????????work ????????????????????????????????????
 *
 * @author Rui
 *
 *         ???????????????????????????????????????
 */
class ActionAssignCreate extends BaseAction {

	@ActionLogger
	private static Logger logger = LoggerFactory.getLogger(ActionAssignCreate.class);

	ActionResult<Wo> execute(EffectivePerson effectivePerson, JsonElement jsonElement) throws Exception {
		ActionResult<Wo> result = new ActionResult<>();
		Wo wo = new Wo();
		Wi wi = this.convertToWrapIn(jsonElement, Wi.class);
		Boolean processing = wi.getProcessing();

		Work work = null;
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			Business business = new Business(emc);
			List<String> applicationIds = listApplication(business, wi.getApplication());
			if (ListTools.isEmpty(applicationIds)) {
				throw new ExceptionEntityNotExist(wi.getApplication(), Application.class);
			}
			Process process = getProcess(business, applicationIds, wi.getProcess());
			Application application = business.element().get(process.getApplication(), Application.class);
			Begin begin = business.element().getBeginWithProcess(process.getId());
			work = create(application, process, begin);
			String identityDn = business.organization().identity().get(wi.getIdentity());
			if (StringUtils.isEmpty(identityDn)) {
				throw new ExceptionIdentityNotExist(wi.getIdentity());
			}
			work.setTitle(wi.getTitle());
			work.setCreatorIdentity(identityDn);
			work.setCreatorPerson(business.organization().person().getWithIdentity(identityDn));
			work.setCreatorUnit(business.organization().unit().getWithIdentity(identityDn));
			if (StringUtils.isNotEmpty(work.getCreatorUnit())) {
				Unit unit = business.organization().unit().getObject(work.getCreatorUnit());
				work.setCreatorUnitLevelName(unit.getLevelName());
			}
			/* ?????????????????????????????????????????????????????? */
			work.setDataChanged(true);
			if (ListTools.isNotEmpty(wi.getAttachmentList())) {
				emc.beginTransaction(Attachment.class);
				/** ??????attachmentList?????????????????? */
				// work.setAttachmentList(new ArrayList<String>());
				for (WiAttachment o : wi.getAttachmentList()) {
					StorageMapping fromMapping = ThisApplication.context().storageMappings().get(Attachment.class,
							o.getStorage());
					if (null == fromMapping) {
						throw new ExceptionFromMappingNotExist(o.getStorage());
					}
					Attachment oldAtt = emc.find(o.getId(), Attachment.class);
					if (null == oldAtt) {
						throw new ExceptionEntityNotExist(o.getId(), Attachment.class);
					}
					StorageMapping toMapping = ThisApplication.context().storageMappings().random(Attachment.class);
					if (null == toMapping) {
						throw new ExceptionToMappingNotExist(Attachment.class);
					}
					Attachment attachment = new Attachment(work, effectivePerson.getDistinguishedName(), o.getSite());
					attachment.setActivity(begin.getId());
					attachment.setActivityName(begin.getName());
					attachment.setActivityType(ActivityType.begin);
					attachment.setActivityToken(work.getActivityToken());
					if(BooleanUtils.isTrue(wi.getAttachmentSoftCopy())){
						attachment.setName(o.getName());
						attachment.setDeepPath(toMapping.getDeepPath());
						attachment.setExtension(StringUtils.lowerCase(StringUtils.substringAfterLast(o.getName(), ".")));
						attachment.setLength(oldAtt.getLength());
						attachment.setStorage(toMapping.getName());
						attachment.setLastUpdateTime(new Date());
						attachment.setFromJob(oldAtt.getJob());
						attachment.setFromId(oldAtt.getId());
						attachment.setFromPath(oldAtt.path());
					}else {
						attachment.saveContent(toMapping, o.readContent(fromMapping), o.getName());
					}
					emc.persist(attachment, CheckPersistType.all);
				}
			}
			emc.beginTransaction(Work.class);
			emc.persist(work, CheckPersistType.all);
			if (null != wi.getData()) {
				WorkDataHelper workDataHelper = new WorkDataHelper(emc, work);
				workDataHelper.update(wi.getData());
			}
			emc.commit();
		}
		MessageFactory.work_create(work);
		// if (BooleanUtils.isTrue(processing)) {
		// ThisApplication.context().applications().putQuery(x_processplatform_service_processing.class,
		// Applications.joinQueryUri("work", work.getId(), "processing"), null,
		// work.getJob());
		// }
		if (BooleanUtils.isTrue(processing)) {
			ProcessingAttributes processingAttributes = new ProcessingAttributes();
			Processing p = new Processing(processingAttributes);
			p.processing(work.getId());
		}

		wo.setId(work.getId());
		result.setData(wo);
		return result;
	}

	public static class Wi extends GsonPropertyObject {

		@FieldDescribe("????????????")
		private String application;
		@FieldDescribe("????????????")
		private String process;
		@FieldDescribe("????????????")
		private String identity;
		@FieldDescribe("??????")
		private String title;
		@FieldDescribe("????????????")
		private Data data;
		@FieldDescribe("??????")
		private List<WiAttachment> attachmentList;
		@FieldDescribe("????????????????????????true????????????????????????????????????????????????????????????????????????.")
		private Boolean attachmentSoftCopy;
		@FieldDescribe("????????????")
		private Boolean processing;

		public String getApplication() {
			return application;
		}

		public void setApplication(String application) {
			this.application = application;
		}

		public String getProcess() {
			return process;
		}

		public void setProcess(String process) {
			this.process = process;
		}

		public String getIdentity() {
			return identity;
		}

		public void setIdentity(String identity) {
			this.identity = identity;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Data getData() {
			return data;
		}

		public void setData(Data data) {
			this.data = data;
		}

		public List<WiAttachment> getAttachmentList() {
			return attachmentList;
		}

		public void setAttachmentList(List<WiAttachment> attachmentList) {
			this.attachmentList = attachmentList;
		}

		public Boolean getProcessing() {
			return processing;
		}

		public void setProcessing(Boolean processing) {
			this.processing = processing;
		}

		public Boolean getAttachmentSoftCopy() {
			return attachmentSoftCopy;
		}

		public void setAttachmentSoftCopy(Boolean attachmentSoftCopy) {
			this.attachmentSoftCopy = attachmentSoftCopy;
		}
	}

	public static class WiAttachment extends Attachment {

		private static final long serialVersionUID = 1954637399762611493L;

		public static List<String> Excludes = new ArrayList<>(JpaObject.FieldsInvisible);

		public static WrapCopier<WiAttachment, Attachment> copier = WrapCopierFactory.wi(WiAttachment.class,
				Attachment.class, null, JpaObject.FieldsUnmodify);

	}

	public static class Wo extends WoId {

	}

	private List<String> listApplication(Business business, String applicationFlag) throws Exception {
		EntityManager em = business.entityManagerContainer().get(Application.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Application> root = cq.from(Application.class);
		Predicate p = cb.equal(root.get(Application_.name), applicationFlag);
		p = cb.or(p, cb.equal(root.get(Application_.alias), applicationFlag));
		p = cb.or(p, cb.equal(root.get(Application_.id), applicationFlag));
		p = cb.or(p, cb.equal(root.get(Application_.applicationCategory), applicationFlag));
		cq.select(root.get(Application_.id)).where(p);
		return em.createQuery(cq).getResultList();
	}

	private Process getProcess(Business business, List<String> applicationIds, String processFlag) throws Exception {
		EntityManager em = business.entityManagerContainer().get(Process.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Process> cq = cb.createQuery(Process.class);
		Root<Process> root = cq.from(Process.class);
		Predicate p = cb.equal(root.get(Process_.name), processFlag);
		p = cb.or(p, cb.equal(root.get(Process_.alias), processFlag));
		p = cb.or(p, cb.equal(root.get(Process_.id), processFlag));
		p = cb.and(p, root.get(Process_.application).in(applicationIds));
		p = cb.and(p, cb.or(cb.isTrue(root.get(Process_.editionEnable)), cb.isNull(root.get(Process_.editionEnable))));
		cq.select(root).where(p).orderBy(cb.desc(root.get(Process_.editionNumber)));
		List<Process> list = em.createQuery(cq).getResultList();
		if (list.isEmpty()) {
			throw new ExceptionEntityNotExist(processFlag, Process.class);
		}
		return list.get(0);
	}

	private Work create(Application application, Process process, Begin begin) throws Exception {
		Date now = new Date();
		Work work = new Work();
		/* ??????????????????????????? */
		work.setDataChanged(false);
		work.setWorkThroughManual(false);
		work.setWorkCreateType(Work.WORKCREATETYPE_ASSIGN);
		work.setApplication(application.getId());
		work.setApplicationName(application.getName());
		work.setApplicationAlias(application.getAlias());
		work.setProcess(process.getId());
		work.setProcessName(process.getName());
		work.setProcessAlias(process.getAlias());
		work.setJob(StringTools.uniqueToken());
		work.setStartTime(now);
		// work.setErrorRetry(0);
		work.setWorkStatus(WorkStatus.start);
		work.setDestinationActivity(begin.getId());
		work.setDestinationActivityType(ActivityType.begin);
		work.setDestinationRoute(null);
		work.setSplitting(false);
		work.setActivityToken(StringTools.uniqueToken());
		return work;
	}

}
