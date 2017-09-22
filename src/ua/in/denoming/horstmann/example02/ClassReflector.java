package ua.in.denoming.horstmann.example02;

import java.io.PrintStream;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class ClassReflector {
    static void print(PrintStream stream, String className) {
        Class cls = ClassReflector.getClassByName(className);
        if (cls == null) {
            stream.printf("Class with '%s' not found", className);
            return;
        }
        ClassReflector.print(stream, cls);
    }

    private static void print(PrintStream stream, Class cls) {
        ClassReflector.printInfo(stream, cls);
    }

    private static Class getClassByName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void printInfo(PrintStream stream, Class cls) {
        String modifiers = Modifier.toString(cls.getModifiers());
        if (modifiers.length() > 0) {
            stream.print(modifiers + " ");
        }
        stream.print("class " + cls.getName());

        Class superClass = cls.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            stream.print(" extends " + superClass.getName());
        }
        stream.print("\n{\n");

        printConstructors(stream, cls);
        stream.println();

        printMethods(stream, cls);
        stream.println();

        printFields(stream, cls);
        stream.println("}");
    }

    /**
     * Prints all constructors of a class
     *
     * @param cls a class
     */
    private static void printConstructors(PrintStream stream, Class cls) {
        Constructor[] constructors = cls.getDeclaredConstructors();
        for (Constructor c : constructors) {
            String name = c.getName();
            stream.print("   ");

            String modifiers = Modifier.toString(c.getModifiers());
            if (modifiers.length() > 0) {
                stream.print(modifiers + " ");
            }
            stream.print(name + "(");

            Class[] paramTypes = c.getParameterTypes();
            for (int j = 0; j < paramTypes.length; j++) {
                if (j > 0) stream.print(", ");
                stream.print(paramTypes[j].getName());
            }
            stream.println(");");
        }
    }

    /**
     * Prints all methods of a class
     *
     * @param cls a class
     */
    private static void printMethods(PrintStream stream, Class cls) {
        Method[] methods = cls.getDeclaredMethods();
        for (Method m : methods) {
            Class retType = m.getReturnType();
            String name = m.getName();
            stream.print("   ");

            String modifiers = Modifier.toString(m.getModifiers());
            if (modifiers.length() > 0) {
                stream.print(modifiers + " ");
            }
            stream.print(retType.getName() + " " + name + "(");

            Class[] paramTypes = m.getParameterTypes();
            for (int j = 0; j < paramTypes.length; j++) {
                if (j > 0) stream.print(", ");
                stream.print(paramTypes[j].getName());
            }
            stream.println(");");
        }
    }

    /**
     * Prints all fields of a class
     *
     * @param cls a class
     */
    private static void printFields(PrintStream stream, Class cls) {
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            Class type = f.getType();
            String name = f.getName();
            stream.print("   ");

            String modifiers = Modifier.toString(f.getModifiers());
            if (modifiers.length() > 0) {
                stream.print(modifiers + " ");
            }

            stream.println(type.getName() + " " + name + ";");
        }
    }
}
