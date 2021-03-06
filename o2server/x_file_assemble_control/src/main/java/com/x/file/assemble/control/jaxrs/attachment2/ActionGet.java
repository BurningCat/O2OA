package com.x.file.assemble.control.jaxrs.attachment2;

import org.apache.commons.lang3.StringUtils;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.file.core.entity.personal.Attachment2;

class ActionGet extends BaseAction {

	ActionResult<Wo> execute(EffectivePerson effectivePerson, String id) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			ActionResult<Wo> result = new ActionResult<>();
			Attachment2 attachment = emc.find(id, Attachment2.class);
			if (null == attachment) {
				throw new ExceptionAttachmentNotExist(id);
			}
			/* 判断文件的所有者是否是当前用户 */
			if (!effectivePerson.isManager() && !StringUtils.equals(effectivePerson.getDistinguishedName(), attachment.getPerson())) {
				throw new ExceptionAttachmentAccessDenied(effectivePerson, attachment);
			}
			Wo wo = Wo.copier.copy(attachment);
			wo.setContentType(this.contentType(false, wo.getName()));
			result.setData(wo);
			return result;
		}
	}

	public static class Wo extends Attachment2 {

		private static final long serialVersionUID = -531053101150157872L;

		static WrapCopier<Attachment2, Wo> copier = WrapCopierFactory.wo(Attachment2.class, Wo.class, null,
				JpaObject.FieldsInvisible);

		@FieldDescribe("文件类型")
		private String contentType;

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

	}
}
