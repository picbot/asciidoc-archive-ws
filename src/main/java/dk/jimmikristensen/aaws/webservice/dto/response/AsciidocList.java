package dk.jimmikristensen.aaws.webservice.dto.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "asciidocs")
@XmlAccessorType(XmlAccessType.FIELD)
public class AsciidocList {

    @XmlElement(name = "asciidoc")
    private List<AsciidocProperties> props = new ArrayList<>();
    
    public List<AsciidocProperties> getProps() {
        return props;
    }
    
    public void addProp(AsciidocProperties prop) {
        props.add(prop);
    }
    
}
