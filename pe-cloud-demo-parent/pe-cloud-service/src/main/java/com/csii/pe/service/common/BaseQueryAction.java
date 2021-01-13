package com.csii.pe.service.common;

import com.csii.pe.action.Action;
import com.csii.pe.action.Executable;
import com.csii.pe.core.Context;
import com.csii.pe.core.PeException;
import com.csii.pe.service.mapper.UserMapper;

import java.util.List;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class BaseQueryAction implements Action, Executable {
    public void execute(Context context) throws PeException {
    }
}
