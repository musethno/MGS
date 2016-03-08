package models



import play.api.db._
import play.api.Play.current

import play.api.i18n.Lang

import anorm._
import anorm.SqlParser._

import java.util.Date
import play.Logger


case class Gallerys(
  id: Pk[Long]
  , filename: Option[String]
  , default: Boolean
  , position: Long
  , cat_id: Long
  , caption: Option[String]
)

case class Map(
  id: Pk[Long]
  , annotation: Option[String]
  , shape: String
  , shape_id: String
)

case class mapDimensions(
   id: Pk[Long]
   , width: String
   , height: String
)

case class mapAnnotation(
   annotation: String
)

case class mapShapes(
   shape: String
)

case class Tours(
	id: Pk[Long]
	, prev: Option[Long]
	, next: Option[Long]
	, nextcomment: Option[String]
	, tournumber: Option[Int]
)

case class Gallery_e(
	caption: Option[String]
)

case class Exhibition(
	id: Pk[Long]
	, name: Option[String]
	, name_en: Option[String]
	, level: Long
	, sub: Option[Long]
	, position: Long
	, date_begin: Option[Date]
	, date_end: Option[Date]
	, comment: Option[String]
	, comment_en: Option[String]
	, status_id: Boolean
	, number: Option[Long]
	, type_id: Option[Long]
	, file: Option[String]
	, file2: Option[String]
	, file3: Option[String]
	, width: Option[String]
	, height: Option[String]
)

case class ExhibitionCat(
	id: Pk[Long]
	, name: Option[String]
	, name_en: Option[String]
	, sub: Option[Long]
	, type_id: Option[Long]
	, number: Option[Long]
)



case class Simple(id: Pk[Long])

case class Search(number: Long)


object Exhibition{

	val types:Seq[(Option[Long], String, Option[String])] = 
	Seq(
		  (None, "menu", None)
		, (Some(1),"text", Some("list-alt"))
		, (Some(3), "images", Some("picture"))
		, (Some(4),"audio", Some("volume-up"))
		, (Some(5),"video", Some("film"))
		, (Some(6),"map", None)
	)

	val typesSelect:Seq[(String, String)] = types.map(a => ({if(a._1.isDefined){a._1.get.toString}else{""}} ->a._2))

	val table = "arborescence"

	val query:String = """
		SELECT id, name, name_en, level, sub, position
		, date_begin, date_end, comment, comment_en, file1, file2, file3
		, status_id, number, type_id, prev, next, nextcomment, tournumber, height, width
		FROM """+table+"""
	"""

	/* get whole ascending branch */
	def branch(id: Long, sort: Boolean = true) : List[Exhibition] = {

		var sub:Option[Long] = Some(id)

		var exhibition:List[Exhibition] = List()

		while(sub.isDefined){

			val a = detail(sub.get)
			Logger.info(a.toString)

			if(a.isDefined){
				sub 				= a.get.sub
				exhibition  	= exhibition :+ a.get
			}
			else{
				sub = None
			}
		}

		// sort by level
		if(sort){
			exhibition.sortBy(_.level)
		}
		else{
			exhibition
		}
	}

	def list(id: Option[Long] = None): List[Exhibition] = {

		val q = query+" WHERE 1"+
				{if(id.isDefined){if(id.get==0){" AND sub IS NULL"}else{" AND sub={sub}"}}else{""}}+
				" ORDER BY position"

		DB.withConnection{implicit c =>
			SQL(q)
			.on(
				'sub -> {if(id.isDefined){id.get}else{0}}
			)
			.as(parser *)
		}
	}

	def listSub(id: Long): List[Exhibition] = {
	
		var sub:Option[Long] = Some(id)
		
		var a: List[Exhibition] = List()
		
			val b = detail(sub.get)
			Logger.info(b.toString)

			val c = b.get.sub


		var level:Option[Long] = None
		list().map{l=>
			
			if(level.isDefined && l.level<=level.get){
				level = None
			}

			if(level.isDefined){
				a= a :+ l
			}


			if(l.id.get==id){
				level = Some(l.level)
			}
		}
		a
	}


	def detail(id: Long): Option[Exhibition] = {
		DB.withConnection{implicit c =>
			SQL(query+" WHERE id={id}")
			.on('id -> id)
			.as(parser singleOpt)
		}
		
	}

	def detailFromPosition(position: Long): Option[Exhibition] = {
		DB.withConnection{implicit c =>
			SQL(query+" WHERE position={position}")
			.on('position -> position)
			.as(parser singleOpt)
		}
	}

	def listBetweenIds(id1: Long, id2: Long = 0): List[Long] = {
		
		var q_next:String = """SELECT id FROM """+table+"""
			WHERE 
				id NOT IN({id1},{id2})
				AND position
		"""

		if(id2 == 0){
			q_next = q_next + 
					""" > (SELECT position FROM """+table+""" WHERE id={id1})"""
		}
		else{
			q_next = q_next + 
					""" BETWEEN (SELECT position FROM """+table+""" WHERE id={id1})
						AND (SELECT position FROM """+table+""" WHERE id={id2})
					"""
		}

		DB.withConnection{implicit c =>
			val rows = SQL(q_next)
			.on(
				'id1 -> id1,
				'id2 -> id2
			)
			.apply()
			.map(row =>
				row[Long]("id")
			)
			.toList

			rows
		}
		
	}

	def listSubAfterId(id: Long, direction: Boolean): List[Long] = {
		
		val q:String = """SELECT id FROM """+table+"""
			WHERE 1
				AND position """+getDirectionString(direction, 1)+""" (SELECT position FROM """+table+""" WHERE id={id})
				AND level > (SELECT level FROM """+table+""" WHERE id={id})
		"""

		DB.withConnection{implicit c =>
			val rows = SQL(q)
			.on(
				'id -> id
			)
			.apply()
			.map(row =>
				row[Long]("id")
			)
			.toList

			rows
		}
	}

	// returns a string with element separated with ,
	def listToSQL(l: List[Long]): Option[String] = {
		if(l.length==0){
			return None
		}

		var r:String="("

		l.map{a=>
			r=r+a.toString+","
		}


		r = r.dropRight(1)+")"

		Some(r)
	}

	val parser = {
		get[Pk[Long]]("id")~
		get[Option[String]]("name")~
		get[Option[String]]("name_en")~
		get[Long]("level")~
		get[Option[Long]]("sub")~
		get[Long]("position")~
		get[Option[Date]]("date_begin")~
		get[Option[Date]]("date_end")~
		get[Option[String]]("comment")~
		get[Option[String]]("comment_en")~
		get[Boolean]("status_id")~
		get[Option[Long]]("number")~
		get[Option[Long]]("type_id")~
		get[Option[String]]("file1")~
		get[Option[String]]("file2")~
		get[Option[String]]("file3")~
		get[Option[String]]("width")~
    get[Option[String]]("height") map {
			case id~name~name_en~level~sub~position~date_begin~date_end~comment~comment_en~status_id~number~type_id~file~file2~file3~width~height => 
			Exhibition(id, name, name_en, level, sub, position, date_begin, date_end, comment, comment_en, status_id, number, type_id, if(file.isDefined){Some(Utils.url+file.get)}else{None}, if(file2.isDefined){Some(Utils.url+file2.get)}else{None}, if(file3.isDefined){Some(Utils.url+file3.get)}else{None}, width, height)
		}
	}

	// if id isDefined -> returns last element of subcategory
	def last(id:Long = 0):Option[Exhibition] = {

		val q:String = query+
						"WHERE "+{if(id>0){"sub={id}"}else{"1"}}+
						" ORDER BY position DESC"+
						" LIMIT 0,1"

		//Logger.info(q)

		DB.withConnection{implicit c =>
			SQL(q)
			.on('id -> id)
			.as(parser singleOpt)
		}
	}

	/**********
	*
	*	alter
	* todo: 
	* add: add without caring of where, find right position (catch element), move
	* idea => do not touch attribute position anywhere but in method move!! <- avoids al lot of errors 
	*
	***********/
	def insertOrUpdate(data: Exhibition, id: Long = 0): Long = {
		

		Logger.info(data.comment.toString)

		// get info about sub
		var sub:Option[Exhibition] 		= None
		var level:Long 						= 1
		var position:Long 					= 1

		if(id==0){

			if(data.sub.isDefined){
				sub = detail(data.sub.get)

				level = sub.get.level + 1

				// get position
				val last_sub:Option[Exhibition] = detailNextPosition(sub.get)

				if(last_sub.isDefined){
					position = last_sub.get.position
				}
				// no previous entry in that level
				else{
					position = last().get.position + 1
				}
			}
			else{
				// get last position
				val lastElement = last()
				if(lastElement.isDefined){
					position = lastElement.get.position + 1
					
				}
			}

			// shift position
			DB.withConnection{implicit c =>
				SQL("UPDATE "+table+" SET position=position+1 WHERE position>={position}")
				.on('position -> position)
				.executeUpdate
			}
		}

		// prepare query
		val query = {if(id>0){"UPDATE "}else{"INSERT INTO "}}+table+
		"""
			SET name={name}
			, name_en={name_en}
			, date_begin={date_begin}
			, date_end={date_end}
			, comment={comment}
			, comment_en={comment_en}
			, status_id={status_id}
			, number={number}
			, type_id={type_id}
      , width={width}
      , height={height}
		"""+
		{
			if(id>0){" WHERE id={id}"}
			else{
				"""
					, level={level}, sub={sub}, position={position}
					, date_added=NOW()
				"""
			}
		}

		// insert/update entry
		DB.withConnection{implicit c =>
			SQL(query)
			.on(
				'name 	-> data.name,
				'name_en 	-> data.name_en,
				'sub 		-> data.sub,
				'level 	-> level,
				'position -> position,
				'date_begin -> data.date_begin,
				'date_end -> data.date_end,
				'comment -> data.comment,
				'comment_en -> data.comment_en,
				'status_id -> data.status_id,
				'number -> data.number, 
				'type_id -> data.type_id,
				'width -> data.width,
				'height -> data.height,
				'id -> id
			)
			.executeUpdate
		}

		Utils.getLastId(table).getOrElse(0)
	}

	// todo: allow changing of sub: update sub, level and position! -> only one function : move(a,b)
	def update(data: Exhibition, id: Long){
		// prepare query 
		val query:String = 
			"UPDATE "+
			table+
			" SET name={name} WHERE id={id}"


		// insert/update entry
		DB.withConnection{implicit c =>
			SQL(query)
			.on(
				'name 	-> data.name,
				'id 		-> id
			)
			.executeUpdate
		}
	}

	/* 
	returns number of entries in submenus 
	*/
	def nSubs(id: Long): Option[Long] = {
	
		val qb:String = "SELECT COUNT(id) as c FROM "+table+" WHERE sub={sub}"

		try{
			DB.withConnection{implicit c=>
				val row = SQL(qb)
				.on('sub -> id)
				.apply().head
						
				Some(row[Long]("c"))
			}
		}
		catch{
			case _ : Throwable => None
		}
	}


	// translate direction to different strings
	def getDirectionString(direction:Boolean, t:Long = 0):String = {

		var r:List[String] = List()

		t match{
			case 1 => {r = List(">","<")}
			case 2 => {r = List("ASC","DESC")}
			case _ => {r = List("+","-")}
		}

		if(direction){
			r(0)
		}
		else{
			r(1)
		}

	}

	def detailNextPosition(a: Exhibition, direction: Boolean = true, boundary: Boolean = true): Option[Exhibition] = {
		
		val q_next:String = query+"""
			WHERE 1
				AND position"""+getDirectionString(direction, 1)+"""{position}
				AND level"""+{if(boundary){"<"}else{""}}+"""={level}
			ORDER BY position """+getDirectionString(direction, 2)+"""
			LIMIT 0,1
		"""

		//
		//level={level}

		DB.withConnection{implicit c =>
			SQL(q_next)
			.on(
				'position 	-> a.position,
				'level 		-> a.level,
				'sub 			-> a.sub
			)
			.as(parser singleOpt)
		}
	}

	/*
		get first parent element
	*/
	def getParent(id: Long): Option[Exhibition] ={

		val a = detail(id)

		if(a.isDefined){
			val q:String = query+" WHERE position<{position} AND level<{level} LIMIT 0,1"

			DB.withConnection{implicit c =>
				SQL(q)
				.on(
					'position -> a.get.position, 
					'level -> a.get.level
				)
				.as(parser singleOpt)
			}
		}
		else{
			None
		}
	}

	/*
		a: origin vertex/node
		b: target node
	*/
	def move(a: Exhibition, b:Exhibition){

		val direction:Boolean = {
			if(a.position>b.position){
				false
			}
			else{
				true
			}
		}

		// end of tail and end of tail target
		val c 								= detailNextPosition(a, true, true)
		val d 								= detailNextPosition(b, true, true)

		// get tails
		var ids:List[Long] 	= List()
		var idsA:List[Long] 	= List()

		if(c.isDefined){
			ids = listBetweenIds(a.id.get, c.get.id.get) :+ a.id.get
			Logger.info("origin (A) "+a.id.get+" and end of tail (C) "+c.get.id.get)
		}
		else{
			ids = listBetweenIds(a.id.get, 0) :+ a.id.get
		}

		if(d.isDefined){
			idsA = listBetweenIds(b.id.get, d.get.id.get) :+ b.id.get
			Logger.info("target (B) "+b.id.get+" and end of tail (D) "+d.get.id.get)
		}
		else{
			idsA = listBetweenIds(b.id.get, 0) :+ b.id.get	
		}
		// end tails

		// sumaary of operations
		Logger.info("tail (A - C) ids: "+ids.toString+", dpos: "+ids.size.toString)
		Logger.info("tail (B - D) "+idsA.toString+", dposA: "+idsA.size.toString)
		Logger.info("move tail of: "+getDirectionString(direction)+idsA.size.toString)
		Logger.info("move target tail of "+getDirectionString(!direction)+ids.size)

		// prepare queries
		val q1:String = "UPDATE "+table+" SET position=position"+getDirectionString(direction)+"{dposA} WHERE id IN"+listToSQL(ids).get+""
		val q2:String = "UPDATE "+table+" SET position=position"+getDirectionString(!direction)+"{dpos} WHERE id IN"+listToSQL(idsA).get+""

		// execute queries
		DB.withConnection{implicit c =>
			SQL(q1)
			.on(
				'dposA -> idsA.size
			)
			.executeUpdate

			SQL(q2)
			.on(
				'dpos -> ids.size
			)
			.executeUpdate	
		}
	}

	/*
		moves a item up or down, within same category
		@arg id: id
		@arg direction: indicates in which direction to move the object (0: down, 1: up) --> transformation d' = 1 - 2d (minus because going up is actually decreasing value of position)

		todo: when it is at the end (b undefined)
	*/
	def moveOneStep(id: Long, direction: Boolean){
		val a = detail(id)

		if(a.isDefined){

			// target
			val b = detailNextPosition(a.get, direction)
			
			if(b.isDefined && b.get.level == a.get.level){
				move(a.get, b.get)
			}			
		}
	}

	def delete(id: Long){

		val a = detail(id)

		// if entry exists and no assoaicted subs
		val nSub = nSubs(id)
		Logger.info("nsub:"+nSub.toString)
		if(a.isDefined && nSub.isDefined && nSub.get==0){

			val qs:String = "UPDATE "+table+" SET position=position-1 WHERE position>{position}"

			DB.withConnection{implicit c =>
				SQL(qs)
				.on('position -> a.get.position)
				.executeUpdate
			}


			val q:String = "DELETE FROM "+table+" WHERE id={id}"

			DB.withConnection{implicit c =>
				SQL(q)
				.on('id -> id)
				.executeUpdate
			}
		}
	}

	def subSelect(id: Option[Long] = None): Seq[(String, String)] = {
		var sub: Seq[(String, String)] = Seq(("","-"))

		list(id).map{l =>
			sub = sub :+ (l.id.get.toString,{">" * l.level.toInt}+" "+l.name )
		}

		sub
	}

	def getIdFromNumber(number: Long): Option[Long] ={
		
		val activeExh = list(Some(0)).filter((a: Exhibition) => a.status_id)

		var activeCat:List[Exhibition] = List()

		activeExh.map{a=>
			activeCat = activeCat :::	listSub(a.id.get)
		}

		val interestCat = activeCat.filter((a:Exhibition) => (a.number.isDefined && a.number.get==number))

		if(interestCat.size>0){
			Some(interestCat.head.id.get)
		}
		else{
			None
		}
	}


	object Tour{

		val table = "arborescence"



		def list(id: Long):List[Tours] = {
			DB.withConnection{implicit c =>
				SQL("SELECT * FROM "+table+" WHERE id={id} ORDER BY position ASC")
				.on('id -> id)
				.as(parser *)
			}
		}

		def detail(id: Long):Tours = {
			DB.withConnection{implicit c =>
				SQL("SELECT * FROM "+table+" WHERE id={id}")
				.on('id -> id)
				.as(parser single)
			}
		}


		val parser = {
			get[Pk[Long]]("id")~
			get[Option[Long]]("prev")~
			get[Option[Long]]("next")~
			get[Option[String]]("nextcomment")~
			get[Option[Int]]("tournumber") map {
				case id~prev~next~nextcomment~tournumber=> 
				Tours(id, prev, next, nextcomment, tournumber)
			}
		}


	}


	object Maps{


		val table = "maps"

				def upload(id: Long, shape_id: String, shape: String){

		      val query = "INSERT INTO museum.maps SET arborescence_id="+id+", shape_id='"+shape_id+"', shape='"+shape+"', date_added=NOW();";

  		// insert/update entry
  		DB.withConnection{implicit c =>
  			SQL(query).executeUpdate
		}
} 
		
	
		
		def insert(id: Long, shape_id: String, shape: String){

		      val query = "INSERT INTO museum.maps SET arborescence_id="+id+", shape_id='"+shape_id+"', shape='"+shape+"', date_added=NOW();";

		    

  		// insert/update entry
  		DB.withConnection{implicit c =>
  			SQL(query).executeUpdate
		  } 
		
	}
		
		def update(id: Long, shape_id: String, shape: String){
  
  	  // prepare query
  		val query = "UPDATE museum.maps SET shape='"+shape+"' WHERE shape_id='"+shape_id+"'";
  		
  		// insert/update entry
  		DB.withConnection{implicit c =>
  			SQL(query).executeUpdate
		  } 
		
	}
	
		def annotate(shape_id: String, annotation: String){
  		   
  	  // prepare query
  
  		val query = "UPDATE museum.maps SET annotation='"+annotation+"' WHERE shape_id='"+shape_id+"'";
  		
  		// insert/update entry
  		DB.withConnection{implicit c =>
  			SQL(query).executeUpdate
  		} 
	}
		
		def delete(shape_id: String){
  	  // prepare query
  		val query = "DELETE FROM museum.maps WHERE shape_id='"+shape_id+"'";
  		// insert/update entry
  		DB.withConnection{implicit c =>
  			SQL(query).executeUpdate
  		} 
	}

		def serve(id: Long) = {
  	  // prepare query
  		val query = "SELECT * FROM "+table+" WHERE arborescence_id='"+id+"'";
  		// Select items
  		DB.withConnection{implicit c =>
  			SQL(query)
  				.on('id -> id)
  				.as(parseShapes *)
  		}
		
	}
		
		def serveSingle(id: Long, shape_id: String) = {
  	  // prepare query
  		val query = "SELECT shape FROM "+table+" WHERE shape_id='"+shape_id+"'";
  		// Select items
  		DB.withConnection{implicit c =>
  			SQL(query)
  				.on('id -> id)
  				.as(parseShapes *)
  		}
		
	}

		val parseShapes = {
			get[String]("shape") map {
				case shape=> 
				shape
			}
		}


		val parseAnnotation = {
			get[String]("annotation") map {
				case annotation=> 
				annotation
			}
		}
		
		val parser = {
			get[Pk[Long]]("arborescence_id")~
			get[Option[String]]("annotation")~
			get[String]("shape")~
			get[String]("shape_id") map {
				case id~annotation~shape~shape_id=> 
				Map(id, annotation, shape, shape_id)
			}
		}
}





	object Gallery{

		val table = "gallery"

		/* move the element up or down 
			id: element ids needs to be moved
			sid: associated id
			upOrDown: {
							(true,	up,	left,		-1), 
							(false,	down,	right,	+1)
						}
		*/
		def move(id: Long, sid: Long, upOrDown: Boolean){
			// retrieve element info
			val d 		= detail(id)
			// retrieve size of sub list
			val slist 	= list(sid)

			// evaluate cases where nothing needs to be done
			if(
				!(!upOrDown && d.position>=slist.size)
				&&
				!(upOrDown && d.position<=1)
			){
				val new_position = d.position + {if(upOrDown){-1}else{1}}

				// get element with new position
				val elem = slist.find{a => a.position==new_position}

				// edit positions
				if(elem.isDefined){
					changePosition(d.id.get, new_position)
					changePosition(elem.get.id.get, d.position)
				}
			}
			
		}

		def changePosition(id: Long, position: Long){
	
			DB.withConnection{implicit c=>
				SQL("UPDATE "+table+" SET position={position} WHERE id={id}")
				.on('id -> id, 'position -> position)
				.executeUpdate
			}
		}
		
		def insertFileName(id: Long, filename: Option[String]){
			DB.withConnection{implicit c=>
				SQL("UPDATE "+table+" SET filename={filename} WHERE id={id}")
				.on('id -> id, 'filename -> filename)
				.executeUpdate
			}
		}


		def insert(id: Long): Option[Long] = {

			import concurrent._
			import ExecutionContext.Implicits._

			val r = new scala.util.Random
			val a = r.nextInt(500)

			// todo add pause of random duration (to overcome concurrency ...)
			
			//future{blocking(
				Thread.sleep(a)
				//)}
			// only a problem for very small image .. should actually work

			// problem with concurrency!!
			// include position
			// 1 retrieve latest position



			val new_position = list(id).size + 1


			val query:String = "INSERT INTO "+table+" SET cat_id={id}, position={position}"

			DB.withConnection{implicit c =>
				SQL(query)
				.on('id -> id, 'position -> new_position)
				.executeUpdate
			}

			if(new_position>=list(id).size+2){
				Logger.info("see!")
			}
			Utils.getLastId(table)
		}

		def list(id: Long):List[Gallerys] = {
			DB.withConnection{implicit c =>
				SQL("SELECT * FROM "+table+" WHERE cat_id={id} ORDER BY position ASC")
				.on('id -> id)
				.as(parser *)
			}
		}

		def detail(id: Long):Gallerys = {
			DB.withConnection{implicit c =>
				SQL("SELECT * FROM "+table+" WHERE id={id}")
				.on('id -> id)
				.as(parser single)
			}
		}

		def delete(id: Long){

			//get position + associated id --> update positions
			val d = detail(id)


			DB.withConnection{implicit c =>
				SQL("UPDATE "+table+" SET position=position-1 WHERE position>{position} AND cat_id={cat_id}")
				.on('position -> d.position, 'cat_id -> d.cat_id)
				.executeUpdate

				SQL("DELETE FROM "+table+" WHERE id={id}")
				.on('id -> id)
				.executeUpdate
			}
		}


                val parser = {
                        get[Pk[Long]]("id")~
                        get[Option[String]]("filename")~
                        get[Long]("position")~
                        get[Long]("cat_id")~
                        get[Option[String]]("caption") map {
                                case id~filename~position~cat_id~caption =>
                                Gallerys(id, filename, false, position, cat_id, caption)
                        }
                }


		def update(id: Long, data: Gallery_e){


			import play.Logger
			Logger.info(data.toString)

			DB.withConnection{implicit c =>
				SQL("""
					UPDATE """+table+"""
					SET caption={caption}
					WHERE id={id}
				""")
				.on(
					'caption -> data.caption,
					'id 		-> id
				)
				.executeUpdate
			}
		}
	}

	object File{
		def insert(id: Long, filename: String, type_id: Option[Long], nr:Long=1){

			val file_field:String= {if(nr==1){"file1"} else if (nr==2){"file2"} else{"file3"}}

			val q :String = "UPDATE "+table+" SET "+file_field+"={filename} "+{if(nr==1){", type_id={type_id}"}else{""}}+" WHERE id={id}"
			DB.withConnection{implicit c=>
				SQL(q)
				.on('id -> id, 'filename -> filename, 'type_id -> type_id)
				.executeUpdate
			}

		}

		def delete(id: Long, nr: Long = 1){

			val file_field:String= {if(nr==1){"file1"} else if(nr==2){"file2"} else{"file3"}}

			val q :String = "UPDATE "+table+" SET "+file_field+"=NULL WHERE id={id}"
			DB.withConnection{implicit c=>
				SQL(q)
				.on('id -> id)
				.executeUpdate
			}
		}
	}
}


/*
CREATE TABLE `gallery` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cat_id` int(11) NOT NULL,
  `caption` varchar(256) DEFAULT NULL,
  `position` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8 
*/
