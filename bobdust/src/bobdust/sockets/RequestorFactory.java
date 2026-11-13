package bobdust.sockets;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import javassist.*;

public abstract class RequestorFactory<T> {
			
	public static <T> T get(Class<T> clazz, String host, int port) 
			throws NoSuchMethodException, 
			IllegalArgumentException, 
			InstantiationException, 
			IllegalAccessException, 
			InvocationTargetException, 
			NotFoundException, 
			CannotCompileException, IOException
	{
		ClassPool pool = ClassPool.getDefault();
		String superClassName = Requestor.class.getName();
		CtClass ctClass = pool.makeClass(String.format("%s_%s", superClassName, clazz.getSimpleName()));
		CtClass superClass = pool.get(Requestor.class.getName());
		CtClass interfaceClass = pool.get(clazz.getName());
		ctClass.setSuperclass(superClass);
		ctClass.setInterfaces(new CtClass[]{interfaceClass});
		
		CtConstructor constructor = CtNewConstructor.make(
				new CtClass[]{pool.get(String.class.getName()), CtClass.intType}, 
				new CtClass[]{pool.get(UnknownHostException.class.getName()), pool.get(IOException.class.getName())},
				ctClass);
		ctClass.addConstructor(constructor);
		
		for(Method m : clazz.getMethods())
		{
			List<CtClass> parameterTypes = new ArrayList<CtClass>();
			var paramTypes = m.getParameterTypes();
			for(Class<?> c : paramTypes)
			{
				parameterTypes.add(pool.get(c.getName()));
			}
			CtClass[] types = new CtClass[parameterTypes.size()];
			parameterTypes.toArray(types);
			var methodName = m.getName();
			CtMethod methodContract = interfaceClass.getDeclaredMethod(methodName, types);
			CtClass returnType = methodContract.getReturnType();
			String body = "super.invoke($args, \"%s\", \"%s\", new String[]{%s}, new String[]{%s});";
			body = String.format(body,
				clazz.getName(),
				methodName,
				Arrays.stream(m.getParameters()).map(p -> String.format("\"%s\"", p.getName())).collect(Collectors.joining(", ")),
				Arrays.stream(paramTypes).map(c -> String.format("\"%s\"", c.getName())).collect(Collectors.joining(", ")));
			if(!returnType.equals(CtClass.voidType))
			{
				body = String.format("return ($r)%s", body);
			}
			body = String.format("{ %s }", body);
			CtMethod method = CtNewMethod.make(
					returnType, 
					methodContract.getName(), 
					types, 
					methodContract.getExceptionTypes(), 
					body, 
					ctClass);
			ctClass.addMethod(method);
		}
		
		ctClass.writeFile();
		Class<?> proxyClass = ctClass.toClass(MethodHandles.lookup());
		
		@SuppressWarnings("unchecked")
		T instance = (T)proxyClass.getConstructor(new Class[]{String.class, int.class}).newInstance(host, port);		
		
		return instance;
	}
	
	private RequestorFactory()
	{		
	}

}
