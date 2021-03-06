MWF.xApplication.process.FormDesigner.Module = MWF.xApplication.process.FormDesigner.Module || {};
MWF.xDesktop.requireApp("process.FormDesigner", "Module.$Element", null, false);
MWF.xApplication.process.FormDesigner.Module.Documenteditor = MWF.FCDocumenteditor = new Class({
	Extends: MWF.FC$Element,
	Implements: [Options, Events],
	options: {
		"style": "default",
		"propertyPath": "../x_component_process_FormDesigner/Module/Documenteditor/documenteditor.html"
	},
	
	initialize: function(form, options){
		this.setOptions(options);
		
		this.path = "../x_component_process_FormDesigner/Module/Documenteditor/";
		this.cssPath = "../x_component_process_FormDesigner/Module/Documenteditor/"+this.options.style+"/css.wcss";

		this._loadCss();
		this.moduleType = "element";
		this.moduleName = "documenteditor";

		this.form = form;
		this.container = null;
		this.containerNode = null;
	},
	
	_createMoveNode: function(){
		this.moveNode = new Element("div", {
			"MWFType": "documenteditor",
			"id": this.json.id,
			"styles": this.css.moduleNodeMove,
			"events": {
				"selectstart": function(){
					return false;
				}
			}
		}).inject(this.form.container);
	},
	_createNode: function(){
		this.node = this.moveNode.clone(true, true);
		this.node.setStyles(this.css.moduleNode);
		this.node.set("id", this.json.id);
		this.node.addEvent("selectstart", function(e){
			e.preventDefault();
		});
	},
	_setEditStyle_custom: function(name, obj, oldValue){

		if (name=="documentTempleteType"){
			if (this.json.documentTempleteType!=oldValue){
				this._resetContent();
			}
		}
		if (name=="documentTempleteUrl"){
			if (this.json.documentTempleteUrl!=oldValue){
				this._resetContent();
			}
		}
		if (name=="documentTempleteName"){
			if (this.json.documentTempleteName!=oldValue){
				this._resetContent();
			}
		}
	},
	// _setEditStyle_custom: function(name){
	// },
	_resetContent: function(){
		if (!this.json.documentTempleteType) this.json.documentTempleteType = "sys";
		if (!this.json.documentTempleteName) this.json.documentTempleteName = "standard";
		this.node.empty();
		var pageNode = new Element("div.doc_layout_page", {"styles": this.css.doc_page}).inject(this.node);
		var pageContentNode = new Element("div.doc_layout_page_content", {"styles": this.css.doc_layout_page_content}).inject(pageNode);

		if (this.json.documentTempleteType=="cus" && this.json.documentTempleteUrl){
			pageContentNode.loadHtml(o2.filterUrl(this.json.documentTempleteUrl), function(){
				// if (this.attachmentTemplete){
				// 	var attNode = pageContentNode.getElement(".doc_layout_attachment_content");
				// 	if (attNode) attNode.empty();
				// }
				// if (callback) callback(control);
			}.bind(this));
		}else{
			o2.getJSON(o2.filterUrl("../x_component_process_FormDesigner/Module/Documenteditor/templete/templete.json"), function(json){
				var o = json[this.json.documentTempleteName];
				if (o){
					pageContentNode.loadHtml(o2.filterUrl("../x_component_process_FormDesigner/Module/Documenteditor/templete/"+o.file), function(){
						// if (this.attachmentTemplete){
						// 	var attNode = pageContentNode.getElement(".doc_layout_attachment_content");
						// 	if (attNode) attNode.empty();
						// }
						// if (callback) callback(control);
					}.bind(this));

				}
			}.bind(this));
		}

	},
	_initModule: function(){
		this._resetContent();

		var templateJson = this.form.dataTemplate["Documenteditor"];
		if (!templateJson){
			var templateUrl = o2.filterUrl("../x_component_process_FormDesigner/Module/Documenteditor/template.json");
			templateJson = MWF.getJSON(templateUrl, null, false);
		}
		if (templateJson) this.json.defaultValue = Object.merge(templateJson.defaultValue, this.json.defaultValue);


		this._setNodeProperty();
        if (!this.form.isSubform) this._createIconAction() ;
		this._setNodeEvent();
	},
	_preprocessingModuleData: function(){
		this.node.clearStyles();
		this.json.recoveryStyles = Object.clone(this.json.styles);
		if (this.json.recoveryStyles) Object.each(this.json.recoveryStyles, function(value, key){
			if ((value.indexOf("x_processplatform_assemble_surface")!=-1 || value.indexOf("x_portal_assemble_surface")!=-1)){
				//?????????????????????
			}else{
				this.node.setStyle(key, value);
				delete this.json.styles[key];
			}
		}.bind(this));
		// var size = this.node.getSize();
		this.node.empty();
		//this.node.getFirst().getFirst().empty();
		//this.node.setStyles(this.css.documentEditorNode);

		this.json.preprocessing = "y";
	},
	_recoveryModuleData: function(){
		if (this.json.recoveryStyles) this.json.styles = this.json.recoveryStyles;
		this.json.recoveryStyles = null;
		this.node.empty();
		this._resetContent();
	}
});
