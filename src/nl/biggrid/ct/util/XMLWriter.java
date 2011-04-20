/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.util;

/**
 *
 * @author Souley
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import nl.biggrid.ct.ui.*;

public class XMLWriter {
    private Document document;
    private Vector<TomoEntity> entities;
    private boolean notSummary = true;
    public XMLWriter(Vector<TomoEntity> entities) {
        this.entities = entities;
        //Get a DOM object
        createDocument();
    }
    
	public void write(final String filename){
		createDOMTree("");
		printToFile(filename);
	}

	public String dom2String(final boolean notSummary, final String walltime){
        this.notSummary = notSummary;
        createDOMTree(walltime);

        OutputFormat format = new OutputFormat(document);
        format.setIndenting(true);
        java.io.StringWriter writer = new java.io.StringWriter();
        XMLSerializer serializer = new XMLSerializer(writer, format);
        try {
            serializer.serialize(document);
		} catch(IOException ie) {
		    ie.printStackTrace();
		}
        return writer.getBuffer().toString();
	}

	/**
	 * Using JAXP in implementation independent manner create a document object
	 * using which we create a xml tree in memory
	 */
	private void createDocument() {
		//get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
            //get an instance of builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            //create an instance of DOM
            document = db.newDocument();
		} catch (ParserConfigurationException pce) {
			//dump it
			System.out.println("Error while trying to instantiate DocumentBuilder " + pce);
			System.exit(1);
		}

	}
    
	/**
	 * The real workhorse which creates the XML structure
	 */
	private void createDOMTree(final String walltime){
		//create the root element <workflow>
		Element rootElt = document.createElement("workflow");
		document.appendChild(rootElt);

        // Add walltime element if argument walltime not empty
        if (!walltime.isEmpty()) {
            Element wtElt = document.createElement("walltime");
            Text wtText = document.createTextNode(walltime);
            wtElt.appendChild(wtText);
            rootElt.appendChild(wtElt);
        }
        // End adding walltime element

        Element processElt = null;
        for (TomoEntity entity : entities) {
            if (entity instanceof ProcessEntity) {
                processElt = createProcessElement((ProcessEntity)entity);
                rootElt.appendChild(processElt);
                break;
            }
        }
        if (processElt != null) {
            for (TomoEntity entity : entities) {
                if (entity instanceof ParamFileEntity) {
                    ParamFileEntity paramFile = (ParamFileEntity)entity;
                    if (paramFile.isInput()) {
                        processElt.appendChild(createInputFileElement(paramFile));
                    } else {
                        processElt.appendChild(createOutputFileElement(paramFile));
                    }
                } else if (entity instanceof ParamDataEntity) {
                    processElt.appendChild(createInputDataElement((ParamDataEntity)entity));
                }
            }
        }
	}

	private Element createGeomElement(TomoEntity entity){
		Element geomElt = document.createElement("geom");

		Element xElt = document.createElement("x");
		Text xText = document.createTextNode(Integer.toString(entity.getGeometry().x));
		xElt.appendChild(xText);
		geomElt.appendChild(xElt);

		Element yElt = document.createElement("y");
		Text yText = document.createTextNode(Integer.toString(entity.getGeometry().y));
		yElt.appendChild(yText);
		geomElt.appendChild(yElt);

		return geomElt;
	}

	/**
	 * Helper method which creates a XML element <Book>
	 * @param b The book for which we need to create an xml representation
	 * @return XML element snippet representing a book
	 */
	private Element createProcessElement(ProcessEntity process){
		Element procElt = document.createElement("process");
		procElt.setAttribute("id", process.getId());

		//create executor element and executor text node and attach it to process element
		Element execElt = document.createElement("command");
		Text execText = document.createTextNode(process.getCommand());
		execElt.appendChild(execText);
		procElt.appendChild(execElt);
		//create geom element and coordinate text nodes and attach them to process element
        if (notSummary) {
            procElt.appendChild(createGeomElement(process));
        }
		return procElt;
	}

	/**
	 * Helper method which creates a XML element <Book>
	 * @param b The book for which we need to create an xml representation
	 * @return XML element snippet representing a book
	 */
	private Element createInputFileElement(ParamFileEntity fileEntity){
		Element inputElt = document.createElement("input");
		inputElt.setAttribute("id", fileEntity.getId());

		//create executor element and executor text node and attach it to process element
        if (notSummary) {
            Element typeElt = document.createElement("type");
            Text typeText = document.createTextNode("file");
            typeElt.appendChild(typeText);
            inputElt.appendChild(typeElt);
        }
		//create executor element and executor text node and attach it to process element
		Element valElt = document.createElement("value");
		Text valText = document.createTextNode(fileEntity.getURL());
		valElt.appendChild(valText);
		inputElt.appendChild(valElt);
		//create geom element and coordinate text nodes and attach them to param element
        if (notSummary) {
            inputElt.appendChild(createGeomElement(fileEntity));
        }
		return inputElt;
	}

	/**
	 * Helper method which creates a XML element <Book>
	 * @param b The book for which we need to create an xml representation
	 * @return XML element snippet representing a book
	 */
	private Element createOutputFileElement(ParamFileEntity fileEntity){
		Element outputElt = document.createElement("output");
		outputElt.setAttribute("id", fileEntity.getId());

		//create executor element and executor text node and attach it to process element
        if (notSummary) {
            Element typeElt = document.createElement("type");
            Text typeText = document.createTextNode("file");
            typeElt.appendChild(typeText);
            outputElt.appendChild(typeElt);
        }
		//create executor element and executor text node and attach it to process element
		Element valElt = document.createElement("value");
		Text valText = document.createTextNode(fileEntity.getURL());
		valElt.appendChild(valText);
		outputElt.appendChild(valElt);
		//create geom element and coordinate text nodes and attach them to param element
        if (notSummary) {
            outputElt.appendChild(createGeomElement(fileEntity));
        }
		return outputElt;
	}

	/**
	 * Helper method which creates a XML element <Book>
	 * @param b The book for which we need to create an xml representation
	 * @return XML element snippet representing a book
	 */
	private Element createInputDataElement(ParamDataEntity dataEntity){
		Element inputElt = document.createElement("input");
		inputElt.setAttribute("id", dataEntity.getId());

		//create executor element and executor text node and attach it to process element
        if (notSummary) {
            Element typeElt = document.createElement("type");
            Text typeText = document.createTextNode((dataEntity.getValues().size()>1)?"array":"string");
            typeElt.appendChild(typeText);
            inputElt.appendChild(typeElt);
        }
		//create executor element and executor text node and attach it to process element
		Element valElt = document.createElement("value");
        String valueText = "";
        for (String item : dataEntity.getValues()) {
            valueText += (item+" ");
        }
		Text valText = document.createTextNode(valueText.trim());
		valElt.appendChild(valText);
		inputElt.appendChild(valElt);
		//create geom element and coordinate text nodes and attach them to param element
        if (notSummary) {
            inputElt.appendChild(createGeomElement(dataEntity));
        }
		return inputElt;
	}

	/**
	 * This method uses Xerces specific classes
	 * prints the XML document to file.
     */
	private void printToFile(final String filename){
		try
		{
			OutputFormat format = new OutputFormat(document);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(
			new FileOutputStream(new File(filename)), format);
			serializer.serialize(document);
		} catch(IOException ie) {
		    ie.printStackTrace();
		}
	}

}
