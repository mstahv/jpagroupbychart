package org.vaadin.demo.jpagroupbychart;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Hit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
            
    private String page = "index.html";
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date hitDate = new Date();

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Date getHitDate() {
        return hitDate;
    }

    public void setHitDate(Date hitDate) {
        this.hitDate = hitDate;
    }


}
