
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/")
public class MultiScreenServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static List<StreamGroup> groups;
	//private static String videosPath = "VIDEOS";
	private static final String videoAbsolutePath = "/home/bruno/SMM/VIDEOS";
	private static final String scriptAbsolutePath = "/home/bruno/SMM/SCRIPT";
	private static final String ffmpegAbsolutePath = "/home/bruno/ffmpeg/ffmpeg";
	private static final String ffserverAbsolutePath = "/home/bruno/ffmpeg/ffserver";
	private static final String ffServerUrl = "http://localhost:8080/";

	public void init(ServletConfig config) throws ServletException {
		groups = new ArrayList<StreamGroup>();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<!DOCTYPE html>");
		out.println("<html lang='en'>");
		out.println("<head>");
		out.println("  	<title>MultiScreenProject</title>");
		out.println("  	<meta charset='ISO-8859-15'>");
		out.println("  	<link href='./css/estilos.css' rel='stylesheet' type='text/css' >");
		out.println("</head>");

		out.println("<body background='./images/background.jpeg' class='bodyStyle'>");
		out.println("  	<h2>MultiScreen</h2>");
		out.println("	<form id='formulario' name='formulario' method='post'>");
		out.println("		Seleccione el grupo de streams o cree uno nuevo");
		out.println("		<div>");
		out.println("		<button type='submit' name='group' class='buttons' value='add'>Add new group</button>");
		for (int i = 0; i < groups.size(); i++) {
			String value = groups.get(i).path;
			out.println("		<button type='submit' name='group' class='buttons' value='" + value + "'>" + value
					+ "</button>");
		}

		out.println("			<input type='hidden' id='page' name='page' value='1'>");
		out.println("		</div>");
		out.println("");
		out.println("</form>");
		out.println("</body>");
		out.println("</html>");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<!DOCTYPE html>");
		out.println("<html lang='en'>");
		out.println("<head>");
		out.println("  	<title>MultiScreenProject</title>");
		out.println("  	<meta charset='ISO-8859-15'>");
		out.println("  	<link href='./css/estilos.css' rel='stylesheet' type='text/css' >");
		out.println("</head>");

		out.println("<body background='./images/background.jpeg' class='bodyStyle'>");
		out.println("  	<h2>MultiScreen</h2>");
		out.println("	<form id='formulario' name='formulario' method='post' class='formStyle'>");

		String create = request.getParameter("create");
		String matrix = request.getParameter("matrix");
		String rows = request.getParameter("rows");
		String columns = request.getParameter("columns");
		String selectedPosition = request.getParameter("selectedPosition");
		String group = request.getParameter("group");
		if (create != null) {
			AskSizeGroupStream(out, create);
		} else if (matrix != null) {
			CreateGroupStream(out, matrix, rows, columns, request);
			ShowDisplayMatrix(out, matrix);
		} else if (selectedPosition != null) {
			VideoPage(out, group, selectedPosition, request);
		} else {
			if (group.equals("add")) {
				ShowFilesList(out, request);
			} else {
				ShowDisplayMatrix(out, group);
			}
		}
		out.println("		</div>");
		out.println("");
		out.println("</form>");
		out.println("</body>");
		out.println("</html>");
	}

	protected void AskSizeGroupStream(PrintWriter out, String path) {
		out.println("		Seleccionar el tamaño de la matriz de pantallas");
		out.println("		<div>");

		out.println("		Número de columnas<input type='number' name='rows' min='1'></div><div>");
		out.println("		Número de filas<input type='number' name='columns' min='2'></div><div>");
		out.println("		</div>");
		out.println("<div>" + "<button type='submit' name='matrix' class='buttons' value='" + path + "'>" + "Siguiente"
				+ "</button>");
		out.println("<input type='hidden' class='buttons' id='group' name='screenNumber' value='" + path + "'>");
	}

	protected void CreateGroupStream(PrintWriter out, String path, String rows, String columns, HttpServletRequest request) {
		int y = Integer.parseInt(rows);
		int x = Integer.parseInt(columns);
		//String path_script = request.getSession().getServletContext().getRealPath("SCRIPT");
		//String path_video = request.getSession().getServletContext().getRealPath(videosPath);
		StreamGroup sGroup = new StreamGroup(path, y, x);
		groups.add(sGroup);
		ProcessBuilder pb = new ProcessBuilder(scriptAbsolutePath+"/videoSpliter.sh", "-i", videoAbsolutePath+"/"+path, "-r", rows, "-c", columns, "--ffmpeg-path", ffmpegAbsolutePath, "--ffserver-path", ffserverAbsolutePath);

		// Map<String, String> env = pb.environment();
		// env.put("VAR1", "myValue");
		// env.remove("OTHERVAR");
		// env.put("VAR2", env.get("VAR1") + "suffix");
		// pb.directory(new File("myDir"));
		try {
		
			//DEBUG MODE
			//pb.redirectErrorStream(true);
			
			
			if(StreamGroup.p==null || ! StreamGroup.p.isAlive()) {
				StreamGroup.p = pb.start();
				System.out.println("Ejecuto");
			}
			
			//DEBUG MODE
			//IOUtils.copy(StreamGroup.p.getInputStream(), System.out);
						
		} catch (IOException e) {
			out.println("Error executing video splitter " + e.getMessage());
		}
	}

	protected void VideoPage(PrintWriter out, String path, String selectedPosition, HttpServletRequest request) {
		if (selectedPosition.equals("none")) {
			Thanks(out, path);
			return;
		}
		else if(selectedPosition.equals("stopStream")) {
			StreamGroup.p.destroy();
			Thanks(out, path);
			return;
		}
		String[] temp = selectedPosition.split("x");
		int x = Integer.parseInt(temp[0]);
		int y = Integer.parseInt(temp[1]);
		out.println("<div>Selected position  " + x + " : " + y + "</div>");
		temp = path.split("\\.");
		String videoLink = ffServerUrl + temp[0] + "_" + x + "_" + y + "." + "webm";//temp[1];
		out.println("<div><video width='1280' height='720' controls autoplay> <source src='" + videoLink + "'"
				+ " type='video/mp4'>Your browser does not support HTML5 video</video></div>");
	}

	protected void Thanks(PrintWriter out, String path) {
		out.println("Thanks for using this service your selected file is " + path);
	}

	protected void ShowFilesList(PrintWriter out, HttpServletRequest request) {
		//String path = request.getSession().getServletContext().getRealPath(videoAbsolutePath);
		File folder = new File(videoAbsolutePath);
		//System.out.println(path);
		out.println("		<div>");
		if (folder.exists()) {
			String buttonDef = "		<button type='submit' name='create' class='buttons' value='";
			for (File file : folder.listFiles()) {
				String value = file.getName();
				out.println(buttonDef + value + "'>" + value + "</button>");
			}
		} else {
			out.println("PLEASE MAKE VIDEOS FOLDER IN: " + videoAbsolutePath);
		}

		out.println("		</div>");
	}

	protected void ShowDisplayMatrix(PrintWriter out, String group) {
		int indx = -1;
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).path.equals(group)) {
				indx = i;
			}
		}
		if (indx < 0) {
			out.println("It was a problem retrieving your stream please try again later");
			return;
		}
		StreamGroup sGroup = groups.get(indx);
		for (int i = 0; i < sGroup.rows; i++) {
			out.println("<div><tr>");
			for (int j = 0; j < sGroup.columns; j++) {
				String aux = (i + 1) + "x" + (j + 1);
				out.println("<td><button type='submit' name='selectedPosition' class='buttons' value='" + aux + "'> "
						+ aux + "</td>");
			}
			out.println("</tr></div>");
		}
		out.println("<td><button type='submit' name='selectedPosition' class='buttons' value='none'>none</td>");
		out.println("<td><button type='submit' name='selectedPosition' class='buttons' value='stopStream'>Stop Stream</td>");
		out.println("<td><input type='hidden' name='group' value='" + group + "'>");
	}

	private void imprimir404(HttpServletResponse response) {

		PrintWriter out;
		try {
			out = response.getWriter();

			out.println("<html>");
			out.println("<!DOCTYPE html>");
			out.println("<html lang='en'>");
			out.println("<head>");
			out.println("  	<title>MultiScreenProject</title>");
			out.println("  	<meta charset='ISO-8859-15'>");
			out.println("  	<link href='./css/estilos.css' rel='stylesheet' type='text/css' >");
			out.println("</head>");

			out.println("<body background='./images/background.jpeg' class='bodyStyle'>");
			out.println("  	<h2>MultiScreen</h2>");
			out.println("	<form id='formulario' name='formulario' method='get' class='formStyle'>");
			out.println("		<div>");
			out.println("		En este momento no existe ninguna peticiï¿½n de reproducciï¿½n multiScreen.");
			out.println("		<button type='submit' name='return' class='buttons'>Volver al principio</button>");

			out.println("		</div>");
			out.println("");
			out.println("</form>");
			out.println("</body>");
			out.println("</html>");
			out.println("<input type='hidden' id='page' name='page' value='1'>");
		} catch (IOException e) {
			System.out.println("No existen flujos!");
		}

	}

	public static class StreamGroup {
		public int rows;
		public int columns;
		public String path;
		public static Process p;

		public StreamGroup(String path, int rows, int columns) {
			this.path = path;
			this.rows = rows;
			this.columns = columns;
		}
	}
}