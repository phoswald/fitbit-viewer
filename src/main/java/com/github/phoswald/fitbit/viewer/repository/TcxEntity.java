package com.github.phoswald.fitbit.viewer.repository;

import static java.util.Objects.requireNonNull;

import java.io.StringReader;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.xml.bind.JAXB;

import com.github.phoswald.fitbit.viewer.tcx.TcxDatabase;

@Entity
@Table(name = "fitbit_tcx_")
@IdClass(TcxEntity.TcxId.class)
public class TcxEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "log_id_", nullable = false)
    private Long logId;

    @Column(name = "tcx_xml_", nullable = false)
    private String tcxXml;

    public static TcxEntity create(String userId, Long logId, String tcxXml) {
        TcxEntity entity = new TcxEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setLogId(requireNonNull(logId, "logId"));
        entity.setTcxXml(tcxXml);
        return entity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getTcxXml() {
        return tcxXml;
    }

    public void setTcxXml(String tcxXml) {
        this.tcxXml = tcxXml;
    }

    public Optional<TcxDatabase> parseTcxXml()  {
        return Optional.ofNullable(tcxXml == null ? null :
                JAXB.unmarshal(new StringReader(tcxXml), TcxDatabase.class));
    }

    public record TcxId(String userId, Long logId) implements java.io.Serializable { }
}
