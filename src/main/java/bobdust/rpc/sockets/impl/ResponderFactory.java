package bobdust.rpc.sockets.impl;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

import bobdust.rpc.sockets.*;
import javassist.*;

public class ResponderFactory {
	private static final ConcurrentHashMap<Integer, ResponderFactory> instances = new ConcurrentHashMap<Integer, ResponderFactory>();
	
	private static ResponderFactory getInstance(int port)
	{
		var key = port;
		ResponderFactory instance;
		synchronized (instances)
		{
			if (instances.containsKey(key))
			{
				instance = instances.get(key);
			}
			else
			{
				instance = new ResponderFactory();
				instances.put(key, instance);
			}
		}
		if (instance != null)
		{
			return instance;
		}
		throw new IllegalStateException("Failed to create responder factory for port " + port);
	}

	public static <T> Responder get(final Class<T> clazz, int port) throws IOException
	{
		var instance = getInstance(port);
		var responder = instance.get(port, clazz);
		return responder;
	}

	private final ConcurrentHashMap<Integer, Object> objects;

	private ResponderFactory()
	{
		objects = new ConcurrentHashMap<Integer, Object>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> SingleResponder<T> get(int port, Class<T> clazz) throws IOException
	{
		instances.put(port, this);
		return (SingleResponder<T>) get(port, p -> {
			try
			{
				return buildResponder(p, clazz, ()-> {
					try
					{
						return clazz.getDeclaredConstructor().newInstance();
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				});
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	private Responder get(int port, IntFunction<Responder> buildResponder)
	{
		var key = port;
		Responder responder;
		synchronized (objects)
		{
			if (objects.containsKey(key))
			{
				responder = (Responder) objects.get(key);
			}
			else
			{
				responder = buildResponder.apply(port);
				if (responder == null)
				{
					throw new IllegalStateException("Failed to create responder for port " + port);
				}
				objects.put(key, responder);
			}
		}
		if (responder != null)
		{
			return responder;
		}
		throw new IllegalStateException("Failed to create responder for port " + port);
	}

	private <T> SingleResponder<T> buildResponder(int port, Class<T> clazz, Supplier<T> factory)
		throws IOException,
		NotFoundException,
		CannotCompileException,
		InstantiationException,
		IllegalAccessException,
		IllegalArgumentException,
		InvocationTargetException,
		NoSuchMethodException
	{
		var pool = ClassPool.getDefault();
		var superClassName = SingleResponderImpl.class.getName();
		var ctClass = pool.makeClass(String.format("%s_%s", superClassName, clazz.getSimpleName()));
		var superClass = pool.get(SingleResponderImpl.class.getName());
		ctClass.setSuperclass(superClass);
		var constructor = CtNewConstructor.make(
				new CtClass[]{CtClass.intType, pool.get(Class.class.getName()), pool.get(Supplier.class.getName())},
				new CtClass[]{pool.get(IOException.class.getName())},
				ctClass);
		ctClass.addConstructor(constructor);
		var proxyClass = ctClass.toClass(MethodHandles.lookup());

		@SuppressWarnings("unchecked")
		var instance = (SingleResponder<T>) proxyClass.getDeclaredConstructor(int.class, Class.class, Supplier.class).newInstance(port, clazz, factory);

		return instance;
	}
}
