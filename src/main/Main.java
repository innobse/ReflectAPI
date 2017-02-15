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
    private static final boolean SERIALIZE = false;
    private static final MyClassLoader loader = new MyClassLoader(ClassLoader.getSystemClassLoader());

    public static void main(String[] args) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        Class<?> classAnimal = null;
        try {
            classAnimal = loader.loadClass("Animal",
                    "https://github.com/innobse/ReflectAPI/raw/master/Animal.jar");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Object monkey = null;

        if (SERIALIZE){
            try {
                monkey = classAnimal.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            serialize(dbf, monkey);
        } else {
            monkey = deserialize(dbf, "sample.xml");
        }

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

            return visit(doc,null , null);

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

    public static Object[] visit(Node node, Class targetClass, Object target) {
        NodeList list = node.getChildNodes();
        Object[] obj = null;
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            obj = process(childNode, targetClass, target);
            obj = visit(childNode, (Class) obj[0], obj[1]);
        }
        return obj;
    }

    public static Object[] process(Node node, Class targetClass, Object target) {
        Object[] result = new Object[2];
        if (node.getNodeName().equals("object")){
            try {
                //targetClass = Class.forName(node.getAttributes().getNamedItem("type").getNodeValue());
                targetClass = loader.loadClass("Animal",
                        "https://github.com/innobse/ReflectAPI/raw/master/Animal.jar");
                target = targetClass.newInstance();
                System.out.println("Создан объект класса " + targetClass.getName());

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
            if (e.getTagName().equals("field")) {
                Field tmpField = null;
                String tmpValue = null;
                for (Field f : targetClass.getDeclaredFields()) {
                    f.setAccessible(true);
                }
                for (int i = 0; i < nnm.getLength(); i++) {
                    Node tmpNode = nnm.item(i);
                    try {
                        switch (tmpNode.getNodeName()) {
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
                    System.out.println("Свойство " + tmpField.getName()
                    + " объекта класса " + targetClass.getName() + " заполнено значением \'"
                    + tmpValue + "\'");
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        }


        result[0] = targetClass;
        result[1] = target;
        return result;
    }

    private static void setValue(Object obj, Field field, String val) throws IllegalAccessException{
        field.setAccessible(true);
        switch(field.getType().getSimpleName().toString()){
            case "String":      field.set(obj, val); break;
            case "Boolean":
            case "boolean":     field.setBoolean(obj, new Boolean(val)); break;
            case "Integer" :
            case "int":         field.setInt(obj, Integer.parseInt(val)); break;
            case "Short" :
            case "short":       field.setShort(obj, Short.parseShort(val)); break;
            case "Byte" :
            case "byte":        field.setByte(obj, Byte.parseByte(val)); break;
            case "Long" :
            case "long":        field.setLong(obj, Long.parseLong(val)); break;
            case "Double" :
            case "double":      field.setDouble(obj, Double.parseDouble(val)); break;
            case "Float" :
            case "float":       field.setFloat(obj, Float.parseFloat(val)); break;
            case "Character" :
            case "char":        field.setChar(obj, val.charAt(0)); break;
            default:            field.set(obj, val); break;
        }
    }
}
