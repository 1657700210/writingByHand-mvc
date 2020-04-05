package com.whpu;

import com.whpu.service.UserService;
import com.whpu.annotation.*;
import com.whpu.annotation.Repository;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;


@WebServlet(urlPatterns = "/", // ƥ�����з���·��
		loadOnStartup = 0, 
		initParams = { @WebInitParam(name = "base-package", value = "com.whpu") }
) // ����Ĭ�ϲ���
public class DispatchServlet extends HttpServlet {
	
	private static final String EMPTY = "";

	//ɨ��Ļ�����
	private String basePackage = EMPTY;
	//ɨ���İ���
	private List<String> packagesName = new ArrayList<String>();
		
	
	//URL�ӿ�-controllerӳ�䷽�� Map����
	private Map<String, Method> urlMethodMap = new HashMap<>();
	
	//controllerӳ�䷽��-��·����ַ Map����
	private Map<Method, String> methodPackageMap = new HashMap<>();
	
	//��·����ַ-ʵ������ Map����
	private Map<String, String> nameMap = new HashMap<>();
	
	//ʵ������-������ʵ������ Map����
	private Map<String, Object> instanceMap = new HashMap<String, Object>();
	/**
	 * ��������ʱ���� ��Ҫ��ɵĹ��ܣ� 
	 * 	1.ɨ�����ȡ������ĵ�ַ
	 *  2.ʵ�������� 
	 *  3.ioc����autowired 
	 *  4.����ӳ��map
	 * 
	 * @throws ServletException
	 */
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		basePackage = config.getInitParameter("base-package");
		try {
			// 1.ɨ�����ȡ������ĵ�ַ
			scanBasePackage(basePackage);
			for(String packagesName1 :packagesName){
				System.out.println("scan_packagesName :  "+packagesName1);
			}
			// 2.ʵ��������
			instance(packagesName);
			for(Map.Entry<String, String> namemap1:nameMap.entrySet()){
				System.out.println("nameMap:   "+nameMap);
			}
			for(Map.Entry<String, Object> instancename:instanceMap.entrySet()){
				System.out.println("instanceMap:   "+instancename);
				
			}
			// 3.ioc����autowired
			ioc();
			// 4.����ӳ��map
			handlerUrlMethod();
			for(Map.Entry<String, Method> namemap1:urlMethodMap.entrySet()){
				System.out.println("urlMethodMap:   "+namemap1);
			}
			for(Map.Entry<Method, String> instancename:methodPackageMap.entrySet()){
				System.out.println("methodPackageMap:   "+instancename);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("��ܼ���ʧ��");
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	/**
	 * �������·����ͨ��������Ƶ��ö�Ӧ��controller��Ķ�Ӧ���� ��Ҫcontroller��ʵ�� �� ��������
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contextPath = req.getContextPath();
		System.out.println("contextPath:   "+contextPath);
		System.out.println( "getRequestURI():  "+ req.getRequestURI());
		System.out.println("---------------");
		String requestURI = req.getRequestURI().replace(contextPath, EMPTY);
		System.out.println("requestURI repalce:   "+requestURI);
		//requesrURI= /user/add
		// for (Entry<String, Method> string : urlMethodMap.entrySet()) {
		// System.out.println(string.getKey());
		// System.out.println(string.getValue());
		// }
		Method method2 = urlMethodMap.get(requestURI);
		Optional<Method> method = Optional.ofNullable(method2);
		method.ifPresent(m -> {
			String pName = methodPackageMap.get(m);
			String obName = nameMap.get(pName);
			Object o = instanceMap.get(obName);
			m.setAccessible(true);
			try {
				m.invoke(o);
			} catch (Exception e) {
				e.printStackTrace();
			}

		});

	}

	/**
	 * ɨ��·���µ������ļ�����ȡ���е���·��
	 * 
	 * @param basePackage
	 */
	public void scanBasePackage(String basePackage) {
		//��ȡcom.whpu����ȫ·����������com.whpu�е� ��.���滻Ϊ��/��
		 URL url = this.getClass().getClassLoader().getResource(basePackage.replace(".", "/"));
		// URL url = this.getClass().getClassLoader().getResource("basePackage");
		System.out.println("scan_url :   "+url);
		
		//��url��Ϊnull������optionalʵ�������򴴽���ʵ��
		Optional<URL> op1 = Optional.ofNullable(url);
		
		// ��ȡurl�е�·����������д���(��url��Ϊ�գ����ظ�ֵ�����򷵻�null)	
		String path = op1.map(URL::getPath).orElse("");
		System.out.println("scan_path :   "+path);
		
		File file = new File(path);
		// listFiles,���ص���File�͵����飬��������ж��Ƿ�Ϊ�ļ���
		Optional<File[]> fileArr = Optional.ofNullable(file.listFiles());
		File[] files2 = fileArr.get();
		for(File obj:files2){
			System.out.println("scan_fileArr:   "+obj);
		}
		
		// �����Ϊ�������
		fileArr.ifPresent(files -> {
			for (File f : files) {
				if (f.isDirectory()) {
					// 2.������ļ�����ݹ����
					//System.out.println(basePackage+" : :  "+f.getName());
					scanBasePackage(basePackage + "." + f.getName());
				} else {
					// 3.������ļ�����·����ַ����packagesName
					 
					packagesName.add(basePackage + "." + f.getName().split("\\.")[0]);
				}
			}
			
		});

	}

	/**
	 * ����ע��ʵ�������� ���Գ��Եݹ�@Component
	 * 
	 * @param packagesNames
	 */
	public void instance(List<String> packagesNames) {
		if (packagesNames.isEmpty()) {
			return;
		}
		// 1.����packagesNames
		packagesNames.forEach(pName -> {
			// �����������һ����.���������ĸת����Сд
			String firstN = pName.substring(pName.lastIndexOf(".") + 1, pName.lastIndexOf(".") + 2).toLowerCase();
			// �����������һ����.��������ַ�ȫ����ȡ��
			String obName = firstN + pName.substring(pName.lastIndexOf(".") + 2);
			System.out.println("instance_obName :  "+obName);
			try {
				Class<?> clazz = Class.forName(pName);
				// 2.�ж������Ƿ����ע��
				// isAnnotationPresent�������Ԫ���ϴ���ָ�����͵�ע�ͣ��򷵻�true�����򷵻�false��
				//���ַ�����Ҫ��Ϊ�˷���ط��ʱ��ע�Ͷ���Ƶġ�
				if (clazz.isAnnotationPresent(Repository.class)) {
					// ���ΪRespositryע����
					String value = clazz.getAnnotation(Repository.class).value();
					if (value.equals(EMPTY)) {
						// ����Զ�������Ϊ�գ�Ĭ��ʹ������Сд
						value = obName;
					}
					//��ע���� ����ʵ���������instanceMap����
					instanceMap.put(value, clazz.newInstance());
					//����·���� ע��������nameMap����
					nameMap.put(pName, value);

				} else if (clazz.isAnnotationPresent(Service.class)) {
					// ���ΪServiceע����
					String value = clazz.getAnnotation(Service.class).value();
					if (value.equals(EMPTY)) {
						// ����Զ�������Ϊ�գ�Ĭ��ʹ������Сд
						value = obName;
					}
					instanceMap.put(value, clazz.newInstance());
					nameMap.put(pName, value);

				} else if (clazz.isAnnotationPresent(Controller.class)) {
					// ���ΪControllerע����
					String value = clazz.getAnnotation(Controller.class).value();
					if (value.equals(EMPTY)) {
						// ����Զ�������Ϊ�գ�Ĭ��ʹ������Сд
						value = obName;
					}
					instanceMap.put(value, clazz.newInstance());
					nameMap.put(pName, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("�����ʧ��");
			}
		});
	}

	/**
	 * ����instanceMap��ʵ�������� �����Ƿ��� ���Ա���Autowired �Զ�ע��
	 *
	 */
	private void ioc() throws IllegalAccessException {
		// 1.����instanceMap
		for (Map.Entry<String, Object> ob : instanceMap.entrySet()) {
			Field[] declaredFields = ob.getValue().getClass().getDeclaredFields();
			for (Field declaredField : declaredFields) {
				// 2.�ж�ʵ�����е������Ƿ���AutoWired
				if (declaredField.isAnnotationPresent(Autowired.class)) {
					String value = declaredField.getAnnotation(Autowired.class).value();
					System.out.println("ioc vlaue:  "+value);
					declaredField.setAccessible(true);
					if (value.equals(EMPTY)) {
						value = declaredField.getName();
					}
					// 3.�о�ʹ��set��������ע�� ������ ��ʵ����ע����ʵ��
					System.out.println("ioc_ob.getValue():  "+ob.getValue());
					System.out.println("ioc_instanceMap.get(value):   "+instanceMap.get(value));
					declaredField.set(ob.getValue(), instanceMap.get(value));

				}

			}

		}

	}

	/**
	 * ��������controller��ı���RequestMapping�ķ������нӿ��뷽�����ӳ��
	 */
	public void handlerUrlMethod() {
		if (packagesName.isEmpty()) {
			return;
		}
		// 1.����packagesName������
		packagesName.forEach(pName -> {
			try {
				Class<?> clazz = Class.forName(pName);
				// 2.�������ɺ���˳�����controller����
				if (clazz.isAnnotationPresent(Controller.class)) {
					String root = "";
					// 3.�ж������Ƿ���RequestMapping
					if (clazz.isAnnotationPresent(RequestMapping.class)) {
						// ���������requestMapping������url����StringBuffer
						root = clazz.getAnnotation(RequestMapping.class).value();

					}
					Method[] methods = clazz.getDeclaredMethods();
					// 4.�жϷ��������Ƿ���RequestMapping
					for (Method method : methods) {
						if (method.isAnnotationPresent(RequestMapping.class)) {
							String url = root + method.getAnnotation(RequestMapping.class).value();
							// ��url�ӿ�ƴ�Ӻ�Ľ�� �� ���������
							urlMethodMap.put(url.toString(), method);
							// �������� �� ��·������
							methodPackageMap.put(method, pName);
						}

					}

				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("��ȡӳ��ʧ��");
			}

		});

	}

	public Map<String, Object> getInstanceMap() {
		return instanceMap;
	}

	public Map<String, String> getNameMap() {
		return nameMap;
	}

	public List<String> getPackagesName() {
		return packagesName;
	}

	public Map<String, Method> getUrlMethodMap() {
		return urlMethodMap;
	}

	public Map<Method, String> getMethodPackageMap() {
		return methodPackageMap;
	}
}
