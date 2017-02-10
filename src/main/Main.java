package main;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Main {

    public static void main(String[] args) {
	    People people = new People();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        serialize(dbf, people);

        People people1 = (People) deserialize(dbf, "sample.xml");

        System.out.println(people1);

        }

        static Object deserialize(DocumentBuilderFactory dbf, String filePath){
            DocumentBuilder builder = null;
            try {
                builder = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = builder.parse(new File(filePath));
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return visit(doc, null);

        }

        static void serialize(DocumentBuilderFactory dbf, Object obj) {
            DocumentBuilder builder = null;
            try {
                builder = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = builder.newDocument();

            Element mainE = doc.createElement("object");
            mainE.setAttribute("type", obj.getClass().getName());
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Element e = doc.createElement("field");
                e.setAttribute("type", field.getType().getSimpleName());
                e.setAttribute("id", field.getName());
                try {
                    e.setAttribute("value", field.get(obj).toString());
                } catch (IllegalAccessException e1) {
                    e.setAttribute("value", "null");
                }
                mainE.appendChild(e);
        }

        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
            Element e = doc.createElement("method");
            e.setAttribute("return-type", method.getReturnType().getSimpleName());
            e.setAttribute("id", method.getName());
            for(Class typeParam : method.getParameterTypes()){
                Element e1 = doc.createElement("param");
                e1.setAttribute("type", typeParam.getSimpleName());
                e.appendChild(e1);
            }
            mainE.appendChild(e);
        }
        doc.appendChild(mainE);

        DOMSource source = new DOMSource(doc);
        StreamResult result = null;
        try {
            result = new StreamResult(new FileOutputStream("sample.xml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transFactory.newTransformer();
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static Object visit(Node node, Object target) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            target = process(childNode, target);
            target = visit(childNode, target);
        }
        return target;
    }

    public static Object process(Node node, Object target) {
        Class targetClass = People.class;
        if (node.getNodeName().equals("object")){
            try {
                targetClass = Class.forName(node.getAttributes().getNamedItem("type").getNodeValue());
                target = targetClass.newInstance();

                //TODO Можно еще допилить для объектов без конструктора по-умолчанию
                //Constructor<?>[] constructors = c.getConstructors();
                //for (int i = 0; i < constructors.length; i++) {
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }


        if (node instanceof Element){
            Element e = (Element) node;
            NamedNodeMap nnm = e.getAttributes();
            if (!e.getTagName().equals("field")) return target;
            Field tmpField = null;
            String tmpValue = null;
            for (Field f : targetClass.getDeclaredFields()) {
                f.setAccessible(true);
            }
            for (int i = 0; i < nnm.getLength(); i++) {
                Node tmpNode = nnm.item(i);
                try {
                    switch(tmpNode.getNodeName()){
                        case "id":
                            tmpField = targetClass.getDeclaredField(tmpNode.getNodeValue());
                            tmpField.setAccessible(true);
                            break;
                        case "value":
                            tmpValue = tmpNode.getNodeValue();
                            break;
                    }
                } catch (NoSuchFieldException e1) {
                    e1.printStackTrace();
                }
            }
            try {
                setValue(target, tmpField, tmpValue);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }

        return target;
    }

    private static void setValue(Object obj, Field field, String val) throws IllegalAccessException{
        field.setAccessible(true);
        switch(field.getType().getSimpleName().toString()){
            case "String":      field.set(obj, val); break;
            case "Integer" :
            case "int":         field.set(obj, Integer.parseInt(val)); break;
            case "Short" :
            case "short":       field.set(obj, Short.parseShort(val)); break;
            case "Byte" :
            case "byte":        field.set(obj, Byte.parseByte(val)); break;
            case "Long" :
            case "long":        field.set(obj, Long.parseLong(val)); break;
            case "Double" :
            case "double":      field.set(obj, Double.parseDouble(val)); break;
            case "Float" :
            case "float":       field.set(obj, Float.parseFloat(val)); break;
            case "Character" :
            case "char":        field.set(obj, val.charAt(0)); break;
            default:            field.set(obj, val); break;
        }
    }
}
