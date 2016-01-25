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
		plugin_insertdate_dateFormat : "%d-%m-%Y",
		//mode : "specific_textareas",
		//	editor_selector : "mceEditor"*/
		
    font_formats: "Andale Mono=andale mono,times;"+
        "Arial=latoregular;"+
        "Arial Black=arial black,avant garde;"+
        "Book Antiqua=book antiqua,palatino;"+
        "Comic Sans MS=comic sans ms,sans-serif;"+
        "Courier New=courier new,courier;"+
        "Georgia=georgia,palatino;"+
        "Helvetica=latoregular;"+
        "Impact=impact,chicago;"+
        "Symbol=symbol;"+
        "Tahoma=tahoma,arial,helvetica,sans-serif;"+
        "Terminal=terminal,monaco;"+
        "Times New Roman=times new roman,times;"+
        "Trebuchet MS=trebuchet ms,geneva;"+
        "Verdana=verdana,geneva;"+
        "Webdings=webdings;"+
        "Wingdings=wingdings,zapf dingbats"

			
});

