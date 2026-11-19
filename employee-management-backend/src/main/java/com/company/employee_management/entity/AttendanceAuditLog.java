package com.company.employee_management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_audit_logs")
public class AttendanceAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_code", length = 64)
    private String terminalCode;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "verification_method", length = 50)
    private String verificationMethod;

    @Column(name = "liveness_passed")
    private Boolean livenessPassed;

    @Column(name = "match_score", precision = 5, scale = 4)
    private BigDecimal matchScore;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "request_nonce", length = 100)
    private String requestNonce;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "server_timestamp", nullable = false)
    private LocalDateTime serverTimestamp = LocalDateTime.now();

    public AttendanceAuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public Boolean getLivenessPassed() { return livenessPassed; }
    public void setLivenessPassed(Boolean livenessPassed) { this.livenessPassed = livenessPassed; }

    public BigDecimal getMatchScore() { return matchScore; }
    public void setMatchScore(BigDecimal matchScore) { this.matchScore = matchScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getRequestNonce() { return requestNonce; }
    public void setRequestNonce(String requestNonce) { this.requestNonce = requestNonce; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public LocalDateTime getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(LocalDateTime serverTimestamp) { this.serverTimestamp = serverTimestamp; }
}
