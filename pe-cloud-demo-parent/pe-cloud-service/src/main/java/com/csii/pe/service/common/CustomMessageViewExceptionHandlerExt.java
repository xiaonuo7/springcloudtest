package com.csii.pe.service.common;

import com.csii.ibs.IbsUser;
import com.csii.pe.channel.http.servlet.ViewExceptionHandler;
import com.csii.pe.core.Context;
import com.csii.pe.core.PeRuntimeException;
import com.csii.uibs.ICachedBankPool;
import com.csii.uibs.rule.ICachedBankRuleAttribute;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import com.csii.pe.accesscontrol.dc.ResubmitControlItem;
import com.csii.pe.accesscontrol.dc.ResubmitException;
import com.csii.pe.core.Context;
import com.csii.pe.core.MessageSourceWrapper;
import com.csii.pe.core.Messageable;
import com.csii.pe.core.OriginalDataInterface;
import com.csii.pe.core.PeException;
import com.csii.pe.core.TransactionConfig;
import com.csii.pe.core.User;
import com.csii.pe.core.ValidationMessage;
import com.csii.pe.validation.MultiValidationRuntimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

public class CustomMessageViewExceptionHandlerExt extends ViewExceptionHandler {
	private ICachedBankRuleAttribute cachedBankRuleAttribute;
	private ICachedBankPool cachedBankPool;
	protected void resolverRejectMessages(MessageSource messageSource, HttpServletRequest request, Locale locale,
			Exception ex, Context context, Map model) {
		ArrayList messages;
		if (ex instanceof MultiValidationRuntimeException) {
			List<ValidationMessage> exceptionList = ((MultiValidationRuntimeException) ex).getExceptionList();
			messages = new ArrayList(exceptionList.size());
			Iterator var9 = exceptionList.iterator();

			while (var9.hasNext()) {
				ValidationMessage vm = (ValidationMessage) var9.next();
				Map exMsgMap = this.resolverRejectMessagesInner(messageSource, request, locale, (Throwable) vm,
						context);
				messages.add(exMsgMap);
			}

			model.putAll((Map) messages.get(0));
		} else {
			Map exMsgMap = this.resolverRejectMessagesInner(messageSource, request, locale, ex, context);
			model.putAll(exMsgMap);
		}

	}
	public Object process(ApplicationContext applContext, HttpServletRequest request, HttpServletResponse response,
			Locale locale, Context context, Exception ex) throws PeException {
		MessageSource messageSource = applContext;
		if (context != null) {
			TransactionConfig tc = context.getTransactionConfig();
			if (tc != null) {
				messageSource = new MessageSourceWrapper(applContext, tc.getApplicationContext());
			}
		}

		HashMap model = new HashMap();
		this.resolverRejectMessages((MessageSource) messageSource, request, locale, ex, context, model);
		request.setAttribute("_viewReferer", "json,");
		return model;
		
	}
	protected String getFieldNameMessage(MessageSource applContext, String id, Locale locale, Context context) {
		if (context == null) {
			return applContext.getMessage(id, (Object[]) null, id, locale);
		} else {
			IbsUser user = (IbsUser) context.getUser();
			String bankId;
			if (user != null) {
				bankId = user.getCif().getBankId();
			} else {
				bankId = context.getString("BankId");
				if (bankId == null) {
					throw new PeRuntimeException("uibs.both_user_and_bankid_is_null");
				}
			}

			try {
				return applContext.getMessage(bankId + "." + id, (Object[]) null, locale);
			} catch (NoSuchMessageException var8) {
				return applContext.getMessage(id, (Object[]) null, id, locale);
			}
		}
	}

	public void setCachedBankPool(ICachedBankPool cachedBankPool) {
		this.cachedBankPool = cachedBankPool;
	}

	public void setCachedBankRuleAttribute(ICachedBankRuleAttribute cachedBankRuleAttribute) {
		this.cachedBankRuleAttribute = cachedBankRuleAttribute;
	}
}