
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;


@WebServlet("/multiscreen")
public class MultiScreenServlet extends HttpServlet implements Runnable {
	private static final long serialVersionUID = 1L;
	private static List<StreamGroup> groups;
	//private static String videosPath = "VIDEOS";
	private static final String videoAbsolutePath = "/home/bruno/SMM/VIDEOS";
	private static final String scriptAbsolutePath = "/home/bruno/SMM/SCRIPT";
	private static final String ffmpegAbsolutePath = "/home/bruno/ffmpeg/ffmpeg";
	private static final String ffserverAbsolutePath = "/home/bruno/ffmpeg/ffserver";
	private static final String ffServerUrl = "http://localhost:8080/";

	private Hashtable<Integer, BigDecimal> timestampHt = new Hashtable<>();
	private int screens=0;
	private List<String> activeUsers=new ArrayList<>();
	
	public void init(ServletConfig config) throws ServletException {
		groups = new ArrayList<StreamGroup>();
	}
	
	public void destroy() {
		if(StreamGroup.p!=null && StreamGroup.p.isAlive()) {
			StreamGroup.p.destroy();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		boolean rest = false;
		if (!request.getParameterMap().isEmpty()) {
			rest = true;
		}
		if (rest) {
			restService(request, response);
		} else {
			out.println("<html>");
			out.println("<!DOCTYPE html>");
			out.println("<html lang='en'>");
			out.println("<head>");
			out.println("  	<title>MultiScreenProject</title>");
			out.println("  	<meta charset='ISO-8859-15'>");
			out.println("  	<link href='./css/estilos.css' rel='stylesheet' type='text/css' >");
			out.println("</head>");
	
			out.println("<body background='./images/background.jpeg' class='bodyStyle'>");
			out.println("   <div id='content'>");
			out.println("  	<h2>MultiScreen</h2>");
			out.println("	<form id='formulario' name='formulario' method='post'>");
			out.println("		Seleccione el grupo de streams o cree uno nuevo");
			out.println("		<div>");
			out.println("		<button type='submit' name='group' class='buttons' value='add'>Add new group</button>");
			for (int i = 0; i < groups.size(); i++) {
				String value = groups.get(i).path;
				String value_show = value;
				if(value.length()>15) {
					value_show = value.substring(0, 14)+"...";
				}
				out.println("		<button type='submit' name='group' class='buttons' value='" + value + "'>" + value_show
						+ "</button>");
			}
	
			out.println("			<input type='hidden' id='page' name='page' value='1'>");
			out.println("		</div>");
			out.println("");
			out.println("</form>");
			out.println("</div>");
			out.println("</body>");
			out.println("</html>");
		}
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
		out.println("  	<link href='css/estilos.css' rel='stylesheet' type='text/css' >");
		out.println("</head>");

		out.println("<body background='./images/background.jpeg' class='bodyStyle'>");
		out.println("   <div id='content'>");
		out.println("  	<h2>MultiScreen</h2>");
		out.println("	<form id='formulario' name='formulario' method='post' class='formStyle'>");

		String create = request.getParameter("create");
		String matrix = request.getParameter("matrix");
		String rows = request.getParameter("rows");
		String columns = request.getParameter("columns");
		String audioStream = request.getParameter("audiostream");
		String audioOptions = request.getParameter("audioOptions");
		String selectedPosition = request.getParameter("selectedPosition");
		String group = request.getParameter("group");
		if (create != null) {
			AskSizeGroupStream(out, create);
		} else if (matrix != null) {
			CreateGroupStream(out, matrix, rows, columns, audioStream, audioOptions, request);
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
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");
	}

	protected void AskSizeGroupStream(PrintWriter out, String path) {
		out.println("		Seleccionar el tamaño de la matriz de pantallas");
		out.println("		<div class='ask'>Número de columnas &nbsp;<input type='number' name='columns' min='1' required></div>");
		out.println("		<div class='ask'>Número de filas &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type='number' name='rows' min='1' required></div>");
		out.println("       <div class='ask'>");
		out.println("            Default Play<input type='radio' name='audioOptions' value='default' checked>");
		out.println("            Stereo Split<input type='radio' name='audioOptions' value='split-audio'>");
		out.println("            Video Mute<input type='radio' name='audioOptions' value='mute-video'>");
		out.println("       </div>");
		out.println("       <div class='ask'>");
		out.println("       Flujo independiente para audio:");
		out.println("       <select name='audiostream'>");
		out.println("          <option value='disabled'>Disabled</option>");
		out.println("          <option value='mono'>Mono</option>");
		out.println("          <option value='stereo'>Stereo</option>");
		out.println("       </select>");
		out.println("       </div>");
		out.println("       <div>");
		out.println("           <button onClick='history.go(-1)' type='submit' class='buttons'>Back</button>");
		out.println("           <button type='submit' name='matrix' class='buttons' value='" + path + "'>" + "Siguiente </button>");
		out.println("           <input type='hidden' class='buttons' id='group' name='screenNumber' value='" + path + "'>");
		out.println("       </div>");
	}

	protected void CreateGroupStream(PrintWriter out, String path, String rows, String columns, String audioStream, String audioOptions, HttpServletRequest request) {
		int y = Integer.parseInt(rows);
		int x = Integer.parseInt(columns);
		//String path_script = request.getSession().getServletContext().getRealPath("SCRIPT");
		//String path_video = request.getSession().getServletContext().getRealPath(videosPath);
		if(groups.size()==0) {
			StreamGroup sGroup = new StreamGroup(path, y, x, audioStream, audioOptions);
			groups.add(sGroup);
		}
		
		screens = x*y;
		
		ProcessBuilder pb = new ProcessBuilder(scriptAbsolutePath+"/videoSpliter.sh", "-i", videoAbsolutePath+"/"+path, "-r", rows, "-c", columns, "--ffmpeg-path", ffmpegAbsolutePath, "--ffserver-path", ffserverAbsolutePath, "--audio-stream", audioStream, "--"+audioOptions);

		// Map<String, String> env = pb.environment();
		// env.put("VAR1", "myValue");
		// env.remove("OTHERVAR");
		// env.put("VAR2", env.get("VAR1") + "suffix");
		// pb.directory(new File("myDir"));
		try {
		
			if(StreamGroup.p==null || ! StreamGroup.p.isAlive()) {
				//DEBUG MODE
				pb.redirectErrorStream(true);
				
				StreamGroup.p = pb.start();
				//System.out.println(pb.command());
				
				//DEBUG MODE
				Thread h = new Thread(this);
				h.start();
			}
						
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
			groups.clear();
			Thanks(out, path);
			return;
		}
		String[] temp;
		String videoLink;
		
		if(selectedPosition.contains("x")) {
			temp = selectedPosition.split("x");
			int x = Integer.parseInt(temp[0]);
			int y = Integer.parseInt(temp[1]);
			temp = path.split("\\.");
			out.println("<div>Selected position  " + x + " : " + y + "</div>");
			videoLink = ffServerUrl + temp[0] + "_" + x + "_" + y + "." + "webm";//temp[1];
		}
		else {
			temp = path.split("\\.");
			videoLink = ffServerUrl + temp[0] + "_" +selectedPosition+ "." + "webm";//temp[1];
		}
		out.println("<div><video id='video' controls autoplay> <source src='" + videoLink + "'"
				+ " type='video/mp4'>Your browser does not support HTML5 video</video></div>");
		out.println("    <script type='text/javascript' src='./js/Servlet.js'></script>");

	}

	protected void Thanks(PrintWriter out, String path) {
		out.println("<p>Thanks for using this service your selected file is " + path + "</p>");
		out.println("<div><button onClick='history.go(-1)' type='submit' class='buttons'>Back</button>");
		out.println("<button onClick=\"document.getElementById(\'formulario\').method=\'get\';\" type='submit' class='buttons'>Volver al inicio</button></div>");
	}

	protected void ShowFilesList(PrintWriter out, HttpServletRequest request) {
		//String path = request.getSession().getServletContext().getRealPath(videoAbsolutePath);
		File folder = new File(videoAbsolutePath);
		//System.out.println(path);
		out.println("		<div>");
		if (folder.exists()) {
			String buttonDef = "		<div><button type='submit' name='create' class='buttonsList' value='";
			for (File file : folder.listFiles()) {
				String value = file.getName();
				String value_show = value;
				if(value.length()>21) {
					value_show = value.substring(0, 20)+"...";
				}
				out.println(buttonDef + value + "'>" + value_show + "</button></div>");
			}
			out.println("   <button onClick='history.go(-1)' type='submit' class='buttons'>Back</button>");
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
			out.println("<div>");
			for (int j = 0; j < sGroup.columns; j++) {
				String aux = (i + 1) + "x" + (j + 1);
				out.println("<div class='movilBoton'>");
				out.println("<button type='submit' type='image' name='selectedPosition' class='buttonsImg' value='" + aux + "'>");
				if(sGroup.audioOptions.equals("split-audio")) {
					if(j==0)
						out.println("<img class='movilspeakerImg' src='./images/movil_speaker_L.png' />");
					else if(j==sGroup.columns-1)
						out.println("<img class='movilspeakerImg' src='./images/movil_speaker_R.png' />");
					else
						out.println("<img class='movilspeakerImg' src='./images/movil_speaker_mute.png' />");
				}
				else if(sGroup.audioOptions.equals("mute-video")) {
					out.println("<img class='movilspeakerImg' src='./images/movil_speaker_mute.png' />");
				}
				else {
					out.println("<img class='movilspeakerImg' src='./images/movil_speaker.png' />");
				}
				out.println("</button>");
				//out.println("<p>"+ aux +"</p>");
				out.println("</div>");
			}
			out.println("</div>");
		}
		if(sGroup.audioStream.equals("mono")) {
			out.println("<p>Flujos solo de audio:</p>");
			out.println("<div class='speakerBoton'><button type='submit' class='buttonsImg' name='selectedPosition' class='buttons' value='MONO'><img class='speakerImg' src='./images/speaker.png'></button></div>");
		}
		if(sGroup.audioStream.equals("stereo")) {
			out.println("<p>Flujos solo de audio:</p>");
			out.println("<div class='speakerBoton'><button type='submit' class='buttonsImg' name='selectedPosition' class='buttons' value='L'><img class='speakerImg' src='./images/speaker_L.png'></button>");
			out.println("<button type='submit' class='buttonsImg' name='selectedPosition' class='buttons' value='L'><img class='speakerImg' src='./images/speaker_R.png'></button></div>");
		}
		out.println("    <button onClick='history.go(-1)' type='submit' class='buttons'>Back</button>");
		out.println("    <button type='submit' name='selectedPosition' class='buttons' value='none'>none</button>");
		out.println("    <button type='submit' name='selectedPosition' class='buttons' value='stopStream'>Stop Stream</button>");
		out.println("    <input type='hidden' name='group' value='" + group + "'>");
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
			out.println("		En este momento no existe ninguna petición de reproducción multiScreen.");
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
		
		protected void restService(HttpServletRequest request, HttpServletResponse response) {
			try {
//				String pos = request.getParameter("pos").toString();
				String timeString = request.getParameter("timestamp").toString();
				BigDecimal timestamp= new BigDecimal(timeString);
				activeUsers.add("1");
				int id= activeUsers.size();
				timestampHt.put(id, timestamp);
				boolean repeat=true;
				String restResponse="";
				while(repeat){
					try {
						Thread.sleep(100);//ms
						if(activeUsers.size()==screens){
							restResponse=checkTimeout(id);
							Thread.sleep(210);
							repeat=false;
						}
					} catch (Exception e) {
						System.out.println("excepcion de sincronizacion");
					}
				}
				activeUsers.remove("1");
				
				PrintWriter out = response.getWriter();
				out.println(restResponse);
		
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private String checkTimeout(int id){
			
			ArrayList<BigDecimal> auxList= new ArrayList<>();
			for(int i=0;i<timestampHt.size();i++){
				auxList.add(timestampHt.get(i+1));
			}
			BigDecimal lower=auxList.get(0);
			for(BigDecimal i: auxList) {
				if(i.compareTo(lower) == -1){
					lower = i;
				}
			}
			
			return timestampHt.get(id).subtract(lower).toString();
		}

	public static class StreamGroup {
		public int rows;
		public int columns;
		public String audioStream;
		public String audioOptions;
		public String path;
		public static Process p;

		public StreamGroup(String path, int rows, int columns, String audioStream, String audioOptions) {
			this.path = path;
			this.rows = rows;
			this.columns = columns;
			this.audioStream = audioStream;
			if(audioOptions==null)
				this.audioOptions = "";
			else
				this.audioOptions = audioOptions;
		}
	}

	@Override
	public void run() {
		try {
			File f = new File("videoSplit.log");
			PrintStream ps = new PrintStream(f);
			IOUtils.copy(StreamGroup.p.getInputStream(), ps);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}