tinyMCE.init({
		mode : "textareas",
		theme : "advanced",
		plugins : "safari,media,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template",
		
		theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,code, formatselect,|,justifyleft,justifycenter,justifyright,justifyfull",//",|,styleselect,formatselect,fontselect,fontsizeselect",
		theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
		//theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
		//theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,spellchecker,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,blockquote,pagebreak,|,insertfile,insertimage",
		theme_advanced_buttons3_add : "emotions",// media, styleprops", // inserttime
		

		theme_advanced_toolbar_location : "bottom",//externsal
		theme_advanced_toolbar_align : "bottom",
		theme_advanced_statusbar_location : "bottom",
		theme_advanced_resizing : true,
		
		
		//content_css : "/css/vbc/main.css" ,
		
		
		dialog_type : "modal",
		language : 'en',
		relative_urls : false,
		//possibility to add iframes
		extended_valid_elements : "iframe[src|width|height|name|align]",
		
		//plugin_insertdate_timeFormat : "%H:%M:%S",
		plugin_insertdate_dateFormat : "%d-%m-%Y"
		//mode : "specific_textareas",
		//	editor_selector : "mceEditor"*/
					
});