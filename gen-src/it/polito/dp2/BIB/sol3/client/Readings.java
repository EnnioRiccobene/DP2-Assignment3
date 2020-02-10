
package it.polito.dp2.BIB.sol3.client;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="self" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="bookshelf" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="numberOfReads" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "self",
    "bookshelf"
})
@XmlRootElement(name = "readings")
public class Readings {

    @XmlSchemaType(name = "anyURI")
    protected String self;
    @XmlSchemaType(name = "anyURI")
    protected String bookshelf;
    @XmlAttribute(name = "numberOfReads", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger numberOfReads;

    /**
     * Gets the value of the self property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSelf() {
        return self;
    }

    /**
     * Sets the value of the self property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSelf(String value) {
        this.self = value;
    }

    /**
     * Gets the value of the bookshelf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookshelf() {
        return bookshelf;
    }

    /**
     * Sets the value of the bookshelf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookshelf(String value) {
        this.bookshelf = value;
    }

    /**
     * Gets the value of the numberOfReads property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfReads() {
        return numberOfReads;
    }

    /**
     * Sets the value of the numberOfReads property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfReads(BigInteger value) {
        this.numberOfReads = value;
    }

}
