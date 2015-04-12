/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.jimmikristensen.aaws.webservice.dto.response.adaptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author jaar
 */
public class DateAdapter extends XmlAdapter<String, Date> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public DateAdapter() {
        dateFormat.setTimeZone(TimeZone.getDefault());
        dateFormat.setLenient(false);
    }

    @Override
    public Date unmarshal(String v) throws Exception {
        if (v != null) {
            return dateFormat.parse(v);
        }
        return null;
    }

    @Override
    public String marshal(Date v) throws Exception {
        if (v != null) {
            return dateFormat.format(v);
        }
        return null;
    }
}
