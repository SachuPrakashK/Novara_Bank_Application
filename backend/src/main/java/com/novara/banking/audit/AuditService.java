package com.novara.banking.audit;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AuditService {
    private final JdbcClient jdbc;
    public AuditService(JdbcClient jdbc){this.jdbc=jdbc;}
    public void record(String actor,String action,String targetType,UUID target,String outcome,String correlationId,String authorizationContextJson){
        jdbc.sql("""
          INSERT INTO audit_events(actor_subject,action,target_type,target_public_id,outcome,correlation_id,authorization_context)
          VALUES(:actor,:action,:targetType,:target,:outcome,:correlation,CAST(:context AS jsonb))
          """).param("actor",actor).param("action",action).param("targetType",targetType).param("target",target).param("outcome",outcome).param("correlation",correlationId).param("context",authorizationContextJson).update();
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String actor,String action,String targetType,String correlationId,String failureCode){
        record(actor,action,targetType,null,"DENIED",correlationId,"{\"failureCode\":\""+safeCode(failureCode)+"\"}");
    }
    private String safeCode(String value){return value!=null&&value.matches("[A-Z0-9_]{1,60}")?value:"UNCLASSIFIED";}
}
