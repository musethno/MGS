package models

import play.api._
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import java.util.Date

import java.io.{ IOException, FileOutputStream, FileInputStream, File }
import java.util.zip.{ ZipEntry, ZipInputStream }

import play.api.templates.Html

case class Site(id: Long, name: String)
case class Currency(id: Long, name: String)

object Extensions {
  // Define a custom String with more methods
  class StringW(s: String) {
    def nl2br = Html(s.replace("\n", "<br>"))
  }


  // Implicit conversion from String to our enriched String
  implicit def pimpString(s: String) = new StringW(s)
}


object Misc{
	// random password pgeneration
	// stolen from     http://www.bindschaedler.com/2012/04/07/elegant-random-string-generation-in-scala/

	// Random generator
	//val random = new scala.util.Random
	val random = new scala.util.Random(new java.security.SecureRandom())


	// Generate a random string of length n from the given alphabet
	def randomString(alphabet: String)(n: Int): String = 
	Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

	// Generate a random alphabnumeric string of length n
	def randomAlphanumericString(n: Int) = 
		randomString("ABCDEFGHIJKLMNOPQRSTUVWXYSabcdefghijklmnopqrstuvwxyz0123456789")(n)
	// end random password generation

}

object Utils{


	val formatter = {
		import java.text.DecimalFormat;
		import java.text.DecimalFormatSymbols;
		import java.text.NumberFormat;
		val symbols= DecimalFormatSymbols.getInstance();
		symbols.setGroupingSeparator('\'');
		
		new DecimalFormat("000",symbols)
	}

	def generatePassword(n:Int = 5):String = {
		import java.security.SecureRandom;
		import java.math.BigInteger;

		val random = new SecureRandom()
		return new BigInteger(130, random).toString(32).substring(0,n)

	}

	// constants
	val path ="assets/data/" // live server path...
	val url = "assets/data/" // live server data file url
	//val path ="/var/www/html/data-museum/" //testing data url in apache public html
	//val url = "http://192.168.1.77/data-museum/" // testing - on port 80 @ arx
	//val url = "http://roggwil.akehir.com/data-museum/" // testing - on port 80 @ mobi
	val site_id = 1
	val admin_fees:Double = 400.0
	

	def flag() : Option[Int] = {
		DB.withConnection { implicit c =>
			val row = 
				SQL(""" 	
					SELECT install_flag
					FROM system
				""")
				.apply().head
					
			row[Option[Int]]("install_flag")
		}
	}

	def returnJSONfromSeq(in: Seq[(String,String)]): String ={
		var out:String =""

		for ((k,v) <- in) 
		{
			out = out+ "{\"k\":\""+k.toString+"\",\"v\":\""+v+"\"},"  
		}  

		// remove last character
		out=out.dropRight(1)

		return "["+out+"]" 
	}

	/*
		duplicate a DB row
		@arg table: table from whic row needs to be copied
		@arg id: id to be copied
		@return: new id
	*/
	def duplicateRow(id: Long, table: String) : Option[Long] = {
		
		val query:String 	= "CREATE TABLE table_temp SELECT * FROM "+table+" WHERE id={id}"
		
		doQuery("DROP TABLE IF EXISTS table_temp")
		
		DB.withConnection{implicit c =>
			SQL(query)
			.on('id -> id) //'
			.executeUpdate()
		}
		
		doQuery("UPDATE table_temp SET id = (1+(SELECT id FROM "+table+" ORDER BY id DESC LIMIT 0,1))")
		doQuery("INSERT INTO "+table+" SELECT * FROM table_temp")
		
		// get new id
		val new_id = getLastId(table)

		doQuery("DROP TABLE IF EXISTS table_temp")
		
		new_id
	}
	
	def doQuery(query: String){
		DB.withConnection{implicit c =>
			SQL(query)
			.executeUpdate()
		}
	}

	def getLastId(table: String) : Option[Long] = {
		DB.withConnection { implicit c =>
			val row = 
				SQL(""" 	
					SELECT id
					FROM """+table+"""
					WHERE 1
					ORDER BY id DESC
					LIMIT 0,1
				""")
				.apply().head
					
			row[Option[Long]]("id")
		}
	}

	object File{
import java.io.{OutputStream, InputStream, File, FileOutputStream}
import java.util.zip.{ZipEntry, ZipFile}
import scala.collection.JavaConversions._

 val BUFSIZE = 4096
  val buffer = new Array[Byte](BUFSIZE)

  def unZip(source: String, targetFolder: String) = {
    val zipFile = new ZipFile(source)

    unzipAllFile(zipFile.entries.toList, getZipEntryInputStream(zipFile)_, new File(targetFolder))
  }

  def getZipEntryInputStream(zipFile: ZipFile)(entry: ZipEntry) = zipFile.getInputStream(entry)

  def unzipAllFile(entryList: List[ZipEntry], inputGetter: (ZipEntry) => InputStream, targetFolder: File): Boolean = {

    entryList match {
      case entry :: entries =>

        if (entry.isDirectory)
          new File(targetFolder, entry.getName).mkdirs
        else
          saveFile(inputGetter(entry), new FileOutputStream(new File(targetFolder, entry.getName)))

        unzipAllFile(entries, inputGetter, targetFolder)
      case _ =>
        true
    }
  }

  def saveFile(fis: InputStream, fos: OutputStream) = {
    writeToFile(bufferReader(fis)_, fos)
    fis.close
    fos.close
  }

  def bufferReader(fis: InputStream)(buffer: Array[Byte]) = (fis.read(buffer), buffer)

  def writeToFile(reader: (Array[Byte]) => Tuple2[Int, Array[Byte]], fos: OutputStream): Boolean = {
    val (length, data) = reader(buffer)
    if (length >= 0) {
      fos.write(data, 0, length)
      writeToFile(reader, fos)
    } else
      true
  }



 		def upload(request: play.api.mvc.Request[play.api.libs.Files.TemporaryFile], id: Long, table: String) : String = {
 			import java.io.File
			val resultString = try {
				val file = new File(getFilename(id, table))
		    	request.body.moveTo(file, true)
				"file has been uploaded"
			} catch {
				case e: Exception => "an error has occurred while uploading the file"
			}

			"{success: true}"
 		}

 		def serve(id: Long, timestamp: String, table: String, fieldname: String = "name") : Option[(String,String)] ={
 			

			try{
				DB.withConnection { implicit c =>

					val row = 
					SQL(""" 	
						SELECT id, timestamp, name
						FROM """+table+"""
						WHERE 1
						AND id={id}
						AND timestamp={timestamp}
						ORDER BY id DESC
						LIMIT 0,1
					""")
					.on(
						'id -> id, 
						'timestamp -> timestamp
					)
					.apply().head
					
					val url = getFilename(row[Long]("id"), table)

					Some(row[String](fieldname),url)

				}
			}
			catch{
				case ex: NoSuchElementException => {
            	None
         	}
         }

 		}

 		def delete(id: Long, table: String) {
 			
 			// 1. remove from database
 			DB.withConnection { implicit c =>
 				SQL("""
 					DELETE FROM """+table+"""
 					WHERE id={id}
 				""")
 				.on('id -> id)
 				.executeUpdate
 			}

 			// 2. remove from server
 			import java.io.File
			val file = new File(getFilename(id, table))
			file.delete()

 		}

 		def getFilename(id: Long, table: String) : String = {
 			println(path+table+"."+id.toString)
 			path+table+"."+id.toString
 		}


 	}

 	object MySQL{
 		def fieldTaken(value:String, field:String ="email", table:String="user"): Boolean = {
		
			val query:String = 
			"""
				SELECT COUNT(*) as c
				FROM """+table+"""
				WHERE """+field+"""={value}
			"""

			DB.withConnection{implicit c =>
				val rows = SQL(query)
				.on('value -> value)
				.apply
				.head
				
				val count = rows[Long]("c")

				if(count==0){
					true
				}
				else{
					false
				}
			}
		}
 	}
}

object ImageUtils {

	import java.awt.Image 
	import java.awt.image.BufferedImage
	import javax.imageio.ImageIO
	import java.awt.Graphics2D
	import java.awt.AlphaComposite
	import java.io.File
	
	def complete(file: java.io.File, path:String){
		val inStream		= new java.io.FileInputStream(file);
		val bufferedImage	= resize(inStream, 200, 200)
		val mini_file		= new File(path);
		
		ImageIO.write(bufferedImage, "jpg", mini_file);
	}
	
    def resize(is:java.io.InputStream, maxWidth:Int, maxHeight:Int):BufferedImage = {
        val originalImage:BufferedImage = ImageIO.read(is)

        var height = originalImage.getHeight
        var width = originalImage.getWidth

        if (width <= maxWidth && height <= maxHeight)
            originalImage
        else {
            // If the picture was too big, it will either fit by width or height.
			// This essentially resizes the dimensions twice, until it fits
	        /*if (width > maxWidth){
	          height = (height.doubleValue() * (maxWidth.doubleValue() / width.doubleValue())).intValue
	          width = maxWidth
	        }
	        if (height > maxHeight){
	          width = (width.doubleValue() * (maxHeight.doubleValue() / height.doubleValue())).intValue
	          height = maxHeight
	        }*/
	
			//Logger.info(width.toString+ " " + height.toString)
	
			var nwidth:Int	= (math.sqrt(width.toDouble/height.toDouble)*100).toInt // correspond à la largeur de l'image souhaitée
			var nheight	= (nwidth.toDouble*height.toDouble/width.toDouble).toInt
			Logger.info("New dimensions: " + nwidth.toString+ " " + nheight.toString)
					
			val scaledBI = new BufferedImage(nwidth, nheight,  BufferedImage.TYPE_INT_RGB)
			val g = scaledBI.createGraphics
			g.setComposite(AlphaComposite.Src)
			g.drawImage(originalImage, 0, 0, nwidth, nheight, null);
			g.dispose
			scaledBI           


        }
    }
}



object Currency{
	def list: List[Currency] = {
		DB.withConnection { implicit connection =>
	      SQL(
	        """
	         	SELECT * 
				FROM currency
				WHERE 1
	        """
	      )
		.as(simple *)
		}
	}
	
	val simple = {
    	get[Long]("id") ~
    	get[String]("short") map {
      		case id~name => Currency(id, name)
    	}
  	}

	def listSelect : Seq[(String,String)] = {
	    list.map(c => c.id.toString -> c.name)
	}
}

object Site{
	def list: List[Site] = {
		DB.withConnection { implicit connection =>
	      SQL(
	        """
	         	SELECT * 
				FROM site
				WHERE 1
	        """
	      )
		.as(simple *)
		}
	}
	
	val simple = {
    	get[Long]("id") ~
    	get[String]("name") map {
      		case id~name => Site(id, name)
    	}
  	}

	def listSelect : Seq[(String,String)] = {
	    list.map(c => c.id.toString -> c.name)
	}
}
