package bobdust.rpc.sockets.impl;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import bobdust.rpc.sockets.*;
import javassist.*;

public class RequestorFactory {
	private static final ConcurrentHashMap<Map.Entry<AddressName, Integer>, RequestorFactory> instances = new ConcurrentHashMap<>();

	public static <T> T get(Class<T> clazz, String host, int port) throws UnknownHostException, IOException {
		var instance = getInstance(host, port);
		return instance.connectChannel(host, port).mount(clazz).As(clazz);
	}

	public static Channel connect(String host, int port) throws UnknownHostException, IOException {
		var instance = getInstance(host, port);
		return instance.connectChannel(host, port);
	}

	private static RequestorFactory getInstance(String host, int port) {
		var key = Map.entry(new AddressName(host), port);
		RequestorFactory instance;
		synchronized (instances) {
			if (instances.containsKey(key)) {
				instance = instances.get(key);
			} else {
				instance = new RequestorFactory();
				instances.put(key, instance);
			}
		}
		return instance;
	}

	private final ConcurrentHashMap<Map.Entry<AddressName, Integer>, Channel> channels;

	private RequestorFactory() {
		channels = new ConcurrentHashMap<>();
	}

	private Channel connectChannel(String host, int port) throws UnknownHostException, IOException {
		instances.put(Map.entry(new AddressName(host), port), this);
		return buildChannel(host, port).connect();
	}

	private Channel buildChannel(String host, int port) {
		var key = Map.entry(new AddressName(host), port);
		Channel channel = null;
		synchronized (channels) {
			if (channels.containsKey(key)) {
				var existingChannel = channels.get(key);
				if (existingChannel instanceof ChannelImpl channelImpl && !channelImpl.isClosed()) {
					channels.remove(key);
				} else {
					channel = channels.get(key);
				}

			}
		}
		if (channel != null) {
			return channel;
		}
		channel = new ChannelImpl(host, port, (clazz, send) -> {
			try {
				return buildRequestor(clazz, send);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		channels.put(key, channel);
		return channel;
	}

	private Object buildRequestor(Class<?> clazz, Function<Command, CommandResult> send)
			throws NotFoundException, CannotCompileException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		ClassPool pool = ClassPool.getDefault();
		String superClassName = Requestor.class.getName();
		CtClass ctClass = pool.makeClass(String.format("%s_%s", superClassName, clazz.getSimpleName()));
		CtClass superClass = pool.get(Requestor.class.getName());
		CtClass interfaceClass = pool.get(clazz.getName());
		ctClass.setSuperclass(superClass);
		ctClass.setInterfaces(new CtClass[] { interfaceClass });

		CtConstructor constructor = CtNewConstructor.make(
				new CtClass[] { pool.get(Function.class.getName()) },
				new CtClass[] {},
				ctClass);
		ctClass.addConstructor(constructor);

		for (Method m : clazz.getMethods()) {
			List<CtClass> parameterTypes = new ArrayList<CtClass>();
			var paramTypes = m.getParameterTypes();
			for (Class<?> c : paramTypes) {
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
					Arrays.stream(m.getParameters()).map(p -> String.format("\"%s\"", p.getName()))
							.collect(Collectors.joining(", ")),
					Arrays.stream(paramTypes).map(c -> String.format("\"%s\"", c.getName()))
							.collect(Collectors.joining(", ")));
			if (!returnType.equals(CtClass.voidType)) {
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

		// ctClass.writeFile();
		var proxyClass = ctClass.toClass(MethodHandles.lookup());

		var instance = proxyClass.getConstructor(new Class[] { Function.class }).newInstance(send);
		return instance;
	}

}
