bind = {};
var library = {
    'version': '4.0',
    "defineProperties": Object.defineProperties || function (obj, properties) {
        function convertToDescriptor(desc) {
            function hasProperty(obj, prop) {
                return Object.prototype.hasOwnProperty.call(obj, prop);
            }

            function isCallable(v) {
                // NB: modify as necessary if other values than functions are callable.
                return typeof v === "function";
            }

            if (typeof desc !== "object" || desc === null)
                throw new TypeError("bad desc");

            var d = {};

            if (hasProperty(desc, "enumerable"))
                d.enumerable = !!obj.enumerable;
            if (hasProperty(desc, "configurable"))
                d.configurable = !!obj.configurable;
            if (hasProperty(desc, "value"))
                d.value = obj.value;
            if (hasProperty(desc, "writable"))
                d.writable = !!desc.writable;
            if (hasProperty(desc, "get")) {
                var g = desc.get;

                if (!isCallable(g) && typeof g !== "undefined")
                    throw new TypeError("bad get");
                d.get = g;
            }
            if (hasProperty(desc, "set")) {
                var s = desc.set;
                if (!isCallable(s) && typeof s !== "undefined")
                    throw new TypeError("bad set");
                d.set = s;
            }

            if (("get" in d || "set" in d) && ("value" in d || "writable" in d))
                throw new TypeError("identity-confused descriptor");

            return d;
        }

        if (typeof obj !== "object" || obj === null)
            throw new TypeError("bad obj");

        properties = Object(properties);

        var keys = Object.keys(properties);
        var descs = [];

        for (var i = 0; i < keys.length; i++)
            descs.push([keys[i], convertToDescriptor(properties[keys[i]])]);

        for (var i = 0; i < descs.length; i++)
            Object.defineProperty(obj, descs[i][0], descs[i][1]);

        return obj;
    },
    'typeOf': function(item){
        if (item == null) return 'null';
        if (item.$family != null) return item.$family();
        if (item.constructor == Array) return 'array';

        if (item.nodeName){
            if (item.nodeType == 1) return 'element';
            if (item.nodeType == 3) return (/\S/).test(item.nodeValue) ? 'textnode' : 'whitespace';
        } else if (typeof item.length == 'number'){
            if (item.callee) return 'arguments';
            //if ('item' in item) return 'collection';
        }

        return typeof item;
    },

    'JSONDecode': function(string, secure){
        if (!string || library.typeOf(string) != 'string') return null;
        return eval('(' + string + ')');
    },

    'JSONEncode': function(obj){
        if (obj && obj.toJSON) obj = obj.toJSON();
        switch (library.typeOf(obj)){
            case 'string':
                return '"' + obj.replace(/[\x00-\x1f\\"]/g, escape) + '"';
            case 'array':
                var string = [];
                for (var i=0; i<obj.length; i++){
                    var json = library.JSONEncode(obj[i]);
                    if (json) string.push(json);
                }
                return '[' + string + ']';
            case 'object': case 'hash':
            var string = [];
            for (key in obj){
                var json = library.JSONEncode(obj[key]);
                if (json) string.push(library.JSONEncode(key) + ':' + json);
            }
            return '{' + string + '}';
            case 'number': case 'boolean': return '' + obj;
            case 'null': return 'null';
        }
        return null;
    }
};
(function(){
    var o={"indexOf": {
        "value": function(item, from){
            var length = this.length >>> 0;
            for (var i = (from < 0) ? Math.max(0, length + from) : from || 0; i < length; i++){
                if (this[i] === item) return i;
            }
            return -1;
        }
    }};
    library.defineProperties(Array.prototype, o);
})();

var wrapWorkContext = {
    "getTask": function(){return library.JSONDecode(workContext.getCurrentTaskCompleted());},
    "getWork": function(){return library.JSONDecode(workContext.getWork());},
    "getActivity": function(){return library.JSONDecode(workContext.getActivity());},
    "getTaskList": function(){return library.JSONDecode(workContext.getTaskList());},
    "getTaskCompletedList": function(){return library.JSONDecode(workContext.getTaskCompletedList());},
    "getReadList": function(){return library.JSONDecode(workContext.getReadList());},
    "getReadCompletedList": function(){return library.JSONDecode(workContext.getReadCompletedList());},
    "getReviewList": function(){return library.JSONDecode(workContext.getReviewList());},
    "getWorkLogList": function(){return library.JSONDecode(workContext.getWorkLogList());},
    "getAttachmentList": function(){return library.JSONDecode(workContext.getAttachmentList());},
    "getRouteList": function(){return library.JSONDecode(workContext.getRouteList());},
    "setTitle": function(title){workContext.setTitle(title);},

    "getControl": function(){return null;},
    "getInquiredRouteList": function(){return null;}
};
//applications


var includedScripts = [];
var _self = this;
var include = function(name, callback){
    if (includedScripts.indexOf(name)==-1){
        var json = library.JSONDecode(_self.workContext.getScript(name, includedScripts));
        includedScripts = includedScripts.concat(json.importedList);
        if (json.text){
            MWF.Macro.exec(json.data.text, bind);
            if (callback) callback.apply(bind);
        }
    }
};

var define = function(name, fun, overwrite){
    var over = true;
    if (overwrite===false) over = false;
    var o = {};
    o[name] = {"value": fun, "configurable": over};
    library.defineProperties(bind, o);
};

var Dict =  function(name){
    var dictionary = _self.dictionary;
    this.name = name;
    this.get = function(path){
        return library.JSONDecode(dictionary.select(this.name, path));
    };
    this.set = function(path, value){
        try {
            dictionary.update(this.name, library.JSONEncode(value), path);
            return true;
        }catch(e){
            return false;
        }
    };
    this.add = function(path, value){
        try {
            dictionary.insert(this.name, library.JSONEncode(value), path);
            return true;
        }catch(e){
            return false;
        }
    };
};
if ((typeof JSON) == 'undefined'){
    JSON = {};
}
JSON.validate = function(string){
    string = string.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').replace(/(?:^|:|,)(?:\s*\[)+/g, '');
    return (/^[\],:{}\s]*$/).test(string);
};
JSON.encode = JSON.stringify ? function(obj){
    return JSON.stringify(obj);
} : function(obj){
    if (obj && obj.toJSON) obj = obj.toJSON();
    switch (typeof obj){
        case 'string':
            return '"' + obj.replace(/[\x00-\x1f\\"]/g, escape) + '"';
        case 'array':
            var string = [];
            for (var i=0; i<obj.length; i++){
                var json = JSON.encode(obj[i]);
                if (json) string.push(json);
            }
            return '[' + string + ']';
        case 'object': case 'hash':
        var string = [];
        for (key in obj){
            var json = JSON.encode(obj[key]);
            if (json) string.push(JSON.encode(key) + ':' + json);
        }
        return '{' + string + '}';
        case 'number': case 'boolean': return '' + obj;
        case 'null': return 'null';
    }
    return null;
};
JSON.decode = function(string, secure){
    if (!string || (typeof string) !== 'string') return null;

    if (secure || JSON.secure){
        if (JSON.parse) return JSON.parse(string);
        if (!JSON.validate(string)) throw new Error('JSON could not decode the input; security is enabled and the value is not secure.');
    }
    return eval('(' + string + ')');
};
var body = {
    set: function(data){
        if ((typeof data)=="string"){
            if (jaxrsBody) jaxrsBody.set(data);
        }else{
            if (jaxrsBody) jaxrsBody.set(JSON.encode(data));
        }
    }
};

var getNameFlag = function(name){
    var t = library.typeOf(name);
    if (t==="array"){
        var v = [];
        name.forEach(function(id){
            v.push((library.typeOf(id)==="object") ? (id.distinguishedName || id.id || id.unique || id.name) : id);
        });
        return v;
    }else{
        return [(t==="object") ? (name.distinguishedName || name.id || name.unique || name.name) : name];
    }
};
var org = {
    "oGroup": this.organization.group(),
    "oIdentity": this.organization.identity(),
    "oPerson": this.organization.person(),
    "oPersonAttribute": this.organization.personAttribute(),
    "oRole": this.organization.role(),
    "oGroup": this.organization.group(),
    "oUnit": this.organization.unit(),
    "oUnitAttribute": this.organization.unitAttribute(),
    "oUnitDuty": this.organization.unitDuty(),

    "group": function() { return this.oGroup},
    "identity": function() { return this.oIdentity},
    "person": function() { return this.oPerson},
    "personAttribute": function() { return this.oPersonAttribute},
    "role": function() { return this.oRole},
    "group": function() { return this.oGroup},
    "unit": function() { return this.oUnit},
    "unitAttribute": function() { return this.oUnitAttribute},
    "unitDuty": function() { return this.oUnitDuty},

    "getObject": function(o, v){
        var arr = [];
        if (!v || !v.length){
            return null;
        }else{
            for (var i=0; i<v.length; i++){
                var g = this.o.getObject???(v[i]);
                if (g) arr.push(g);
            }
        }
        return arr;
    },
    //??????***************
    //????????????--???????????????????????????
    getGroup: function(name){
        var v = this.oGroup.listObject???(getNameFlag(name));
        if (!v || !v.length) v = null;
        return (v && v.length===1) ? v[0] : v;
    },

    //??????????????????--???????????????????????????
    //nested  ??????  true???????????????false?????????????????????false???
    listSubGroup: function(name, nested){
        var v = null;
        if (nested){
            var v = this.oGroup.listWithGroupSubNested(getNameFlag(name));
        }else{
            var v = this.oGroup.listWithGroupSubDirect(getNameFlag(name));
        }
        return this.getObject(this.oGroup, v);
    },
    //??????????????????--???????????????????????????
    //nested  ??????  true???????????????false?????????????????????false???
    listSupGroup:function(name, nested){
        var v = null;
        if (nested){
            var v = this.oGroup.listWithGroupSupNested(getNameFlag(name));
        }else{
            var v = this.oGroup.listWithGroupSupDirect(getNameFlag(name));
        }
        return this.getObject(this.oGroup, v);
    },
    //??????????????????????????????--???????????????????????????
    listGroupWithPerson:function(name){
        var v = this.oGroup.listWithPerson(getNameFlag(name));
        return this.getObject(this.oGroup, v);
    },
    //????????????????????????--??????true, false
    groupHasRole: function(name, role){
        nameFlag = (library.typeOf(name)==="object") ? (name.distinguishedName || name.id || name.unique || name.name) : name;
        return this.oGroup.hasRole(nameFlag, getNameFlag(role));
    },

    //??????***************
    //????????????--???????????????????????????
    getRole: function(name){
        var v = this.oRole.listObject(getNameFlag(name));
        if (!v || !v.length) v = null;
        return (v && v.length===1) ? v[0] : v;
    },
    //??????????????????????????????--???????????????????????????
    listRoleWithPerson:function(name){
        var v = this.oRole.listWithPerson???(getNameFlag(name));
        return this.getObject(this.oRole, v);
    },

    //??????***************
    //????????????????????????--??????true, false
    personHasRole: function(name, role){
        nameFlag = (library.typeOf(name)==="object") ? (name.distinguishedName || name.id || name.unique || name.name) : name;
        return this.oPerson.hasRole(nameFlag, getNameFlag(role));
    },
    //????????????--???????????????????????????
    getPerson: function(name){
        var v = this.oPerson.listObject(getNameFlag(name));
        if (!v || !v.length) v = null;
        return (v && v.length===1) ? v[0] : v;
    },
    //??????????????????--???????????????????????????
    //nested  ??????  true???????????????false?????????????????????false???
    listSubPerson: function(name, nested){
        var v = null;
        if (nested){
            var v = this.oPerson.listWithPersonSubNested(getNameFlag(name));
        }else{
            var v = this.oPerson.listWithPersonSubDirect(getNameFlag(name));
        }
        return this.getObject(this.oPerson, v);
    },
    //??????????????????--???????????????????????????
    //nested  ??????  true???????????????false?????????????????????false???
    listSupPerson: function(name, nested){
        var v = null;
        if (nested){
            var v = this.oPerson.listWithPersonSupNested(getNameFlag(name));
        }else{
            var v = this.oPerson.listWithPersonSupDirect(getNameFlag(name));
        }
        return this.getObject(this.oPerson, v);
    },
    //???????????????????????????--???????????????????????????
    listPersonWithGroup: function(name){
        var v = this.oPerson.listWithGroup(getNameFlag(name));
        if (!v || !v.length) v = null;
        return v;
    },
    //???????????????????????????--???????????????????????????
    listPersonWithRole: function(name){
        var v = this.oPerson.listWithRole???(getNameFlag(name));
        return this.getObject(this.oPerson, v);
    },
    //???????????????????????????--???????????????????????????
    listPersonWithIdentity: function(name){
        var v = this.oPerson.listWithIdentity(getNameFlag(name));
        return this.getObject(this.oPerson, v);
    },
    //???????????????????????????--???????????????????????????
    getPersonWithIdentity: function(name){
        var v = this.oPerson.listWithIdentity(getNameFlag(name));
        var arr = this.getObject(this.oPerson, v);
        return (arr && arr.length) ? arr[0] : null;
    },
    //???????????????????????????--???????????????????????????
    //nested  ??????  true????????????????????????false?????????????????????false???
    listPersonWithUnit: function(name, nested){
        var v = null;
        if (nested){
            var v = this.oPerson.listWithUnitSubNested(getNameFlag(name));
        }else{
            var v = this.oPerson.listWithUnitSubDirect(getNameFlag(name));
        }
        return this.getObject(this.oPerson, v);
    },

    //????????????************
    //?????????????????????(??????????????????values?????????????????????????????????????????????)
    appendPersonAttribute: function(person, attr, values){
        var personFlag = (library.typeOf(person)==="object") ? (person.distinguishedName || person.id || person.unique || person.name) : person;
        return this.oPersonAttribute.appendWithPersonWithName(personFlag, attr, values);
    },
    //?????????????????????(?????????????????????values??????????????????????????????????????????)
    setPersonAttribute: function(person, attr, values){
        var personFlag = (library.typeOf(person)==="object") ? (person.distinguishedName || person.id || person.unique || person.name) : person;
        return this.oPersonAttribute.setWithPersonWithName(personFlag, attr, values);
    },
    //?????????????????????
    getPersonAttribute: function(person, attr){
        var personFlag = (library.typeOf(person)==="object") ? (person.distinguishedName || person.id || person.unique || person.name) : person;
        return this.oPersonAttribute.listAttributeWithPersonWithName(personFlag, attr);
    },
    //?????????????????????????????????
    listPersonAttributeName: function(name){
        var p = getNameFlag(name);
        var nameList = [];
        for (var i=0; i<p.length; i++){
            var v = this.oPersonAttribute.listNameWithPerson(p[i]);
            if (v && v.length){
                for (var j=0; j<v.length; j++){
                    if (nameList.indexOf(v[j])==-1) nameList.push(v[j]);
                }
            }
        }
        return nameList;
    },
    //???????????????????????????
    listPersonAllAttribute: function(name){
        // getOrgActions();
        // var data = {"personList":getNameFlag(name)};
        // var v = null;
        // orgActions.listPersonAllAttribute(data, function(json){v = json.data;}, null, false);
        // return v;
    },

    //??????**********
    //????????????
    getIdentity: function(name){
        var v = this.oIdentity.listObject???(getNameFlag(name));
        if (!v || !v.length) v = null;
        return (v && v.length===1) ? v[0] : v;
    },
    //?????????????????????
    listIdentityWithPerson: function(name){
        var v = this.oIdentity.listWithPerson???(getNameFlag(name));
        return this.getObject(this.oIdentity, v);
    },
    //????????????????????????--???????????????????????????
    //nested  ??????  true????????????????????????false?????????????????????false???
    listIdentityWithUnit: function(name, nested){
        var v = null;
        if (nested){
            var v = this.oIdentity.listWithUnitSubNested(getNameFlag(name));
        }else{
            var v = this.oIdentity.listWithUnitSubDirect(getNameFlag(name));
        }
        return this.getObject(this.oIdentity, v);
    },

    //??????**********
    //????????????
    getUnit: function(name){
        var v = this.oUnit.listObject(getNameFlag(name));
        if (!v || !v.length) v = null;
        return (v && v.length===1) ? v[0] : v;
    },
    //?????????????????????--???????????????????????????
    //nested  ??????  true???????????????false?????????????????????false???
    listSubUnit: function(name, nested){
        var v = null;
        if (nested){
            var v = this.oUnit.listWithUnitSubNested(getNameFlag(name));
        }else{
            var v = this.oUnit.listWithUnitSubDirect(getNameFlag(name));
        }
        return this.getObject(this.oUnit, v);
    },
    //?????????????????????--???????????????????????????
    //nested  ??????  true???????????????false?????????????????????false???
    listSupUnit: function(name, nested){
        var v = null;
        if (nested){
            var v = this.oUnit.listWithUnitSupNested(getNameFlag(name));
        }else{
            var v = this.oUnit.listWithUnitSupDirect(getNameFlag(name));
        }
        return this.getObject(this.oUnit, v);
    },
    //??????????????????????????????
    //flag ??????    ??????????????????????????????
    //     ?????????  ?????????????????????????????????
    //     ???     ?????????????????????????????????
    getUnitByIdentity: function(name, flag){
        getOrgActions();
        var getUnitMethod = "current";
        var v;
        if (flag){
            if (library.typeOf(flag)==="string") getUnitMethod = "type";
            if (library.typeOf(flag)==="number") getUnitMethod = "level";
        }
        var n = getNameFlag(name)[0];
        switch (getUnitMethod){
            case "current":
                v = this.oUnit.getWithIdentity(n);
                break;
            case "type":
                v = this.oUnit.getWithIdentityWithType(n, flag);
                break;
            case "level":
                v = this.oUnit.getWithIdentityWithLevel(n, flag);
                break;
        }
        var o = this.oUnit.getObject(v);
        return o;
    },
    //?????????????????????????????????????????????
    listAllSupUnitWithIdentity: function(name){
        var v = this.oUnit.listWithIdentitySupNested(getNameFlag(name));
        return this.getObject(this.oUnit, v);
    },
    //?????????????????????????????????
    listUnitWithPerson: function(name){
        var v = this.oUnit.listWithPerson(getNameFlag(name));
        return this.getObject(this.oUnit, v);
    },
    //?????????????????????????????????????????????
    listAllSupUnitWithPerson: function(name){
        var v = this.oUnit.listWithPersonSupNested(getNameFlag(name));
        return this.getObject(this.oUnit, v);
    },
    //????????????????????????????????????????????????
    listUnitWithAttribute: function(name, attribute){
        var v = this.oUnit.listWithUnitAttribute(name, attribute);
        return this.getObject(this.oUnit, v);
    },
    //????????????????????????????????????????????????
    listUnitWithDuty: function(name, id){
        var idflag = (library.typeOf(id)==="object") ? (id.distinguishedName || id.id || id.unique || id.name) : id;
        var v = this.oUnit.listWithUnitDuty(name, idflag);
        return this.getObject(this.oUnit, v);
    },

    //????????????***********
    //????????????????????????????????????
    getDuty: function(duty, id){
        var unit = (library.typeOf(id)==="object") ? (id.distinguishedName || id.id || id.unique || id.name) : id;
        var v = this.oUnitDuty.listIdentityWithUnitWithName(unit, duty);
        return this.getObject(this.oIdentity, v);
    },



    //?????????????????????????????????
    listDutyNameWithIdentity: function(name){
        var ids = getNameFlag(name);
        var nameList = [];
        for (var i=0; i<ids.length; i++){
            var v = this.oUnitDuty.listNameWithIdentity(ids[i]);
            if (v && v.length){
                for (var j=0; j<v.length; j++){
                    if (nameList.indexOf(v[j])==-1) nameList.push(v[j]);
                }
            }
        }
        return nameList;
    },
    //?????????????????????????????????
    listDutyNameWithUnit: function(name){
        var ids = getNameFlag(name);
        var nameList = [];
        for (var i=0; i<ids.length; i++){
            var v = this.oUnitDuty.listNameWithUnit(ids[i]);
            if (v && v.length){
                for (var j=0; j<v.length; j++){
                    if (nameList.indexOf(v[j])==-1) nameList.push(v[j]);
                }
            }
        }
        return nameList;
    },
    //???????????????????????????
    listUnitAllDuty: function(name){
        // getOrgActions();
        // var data = {"unitList":getNameFlag(name)};
        // var v = null;
        // orgActions.listUnitAllDuty(data, function(json){v = json.data;}, null, false);
        // return v;
    },

    //????????????**************
    //?????????????????????(??????????????????values?????????????????????????????????????????????)
    appendUnitAttribute: function(unit, attr, values){
        var unitFlag = (library.typeOf(unit)==="object") ? (unit.distinguishedName || unit.id || unit.unique || unit.name) : unit;
        return this.oUnitAttribute.appendWithUnitWithName(unitFlag, attr, values);
    },
    //?????????????????????(?????????????????????values??????????????????????????????????????????)
    setUnitAttribute: function(unit, attr, values){
        var unitFlag = (library.typeOf(unit)==="object") ? (unit.distinguishedName || unit.id || unit.unique || unit.name) : unit;
        return this.oUnitAttribute.setWithUnitWithName(unitFlag, attr, values);
    },
    //?????????????????????
    getUnitAttribute: function(unit, attr){
        var unitFlag = (library.typeOf(unit)==="object") ? (unit.distinguishedName || unit.id || unit.unique || unit.name) : unit;
        return this.oUnitAttribute.listAttributeWithUnitWithName(unitFlag, attr);
    },
    //?????????????????????????????????
    listUnitAttributeName: function(name){
        var p = getNameFlag(name);
        var nameList = [];
        for (var i=0; i<p.length; i++){
            var v = this.oUnitAttribute.listNameWithUnit???(p[i]);
            if (v && v.length){
                for (var j=0; j<v.length; j++){
                    if (nameList.indexOf(v[j])==-1) nameList.push(v[j]);
                }
            }
        }
        return nameList;
    },
    //???????????????????????????
    listUnitAllAttribute: function(name){
        // getOrgActions();
        // var data = {"unitList":getNameFlag(name)};
        // var v = null;
        // orgActions.listUnitAllAttribute(data, function(json){v = json.data;}, null, false);
        // return v;
    }

};
var restfulAcpplication = this.applications;
var Action = (function(){
    var actions = [];
    return function(root, json){
        if (!actions[root]) actions[root] = {};
        Object.keys(json).forEach(function(key){
            actions[root][key] = json[key];
        });
        //Object.merge(actions[root], json);
        this.root = root;
        this.actions = actions[root];
        this.invoke = function(option){
            // {
            //     "name": "",
            //     "data": "",
            //     "parameter": "",,
            //     "success": function(){}
            //     "failure": function(){}
            // }
            if (this.actions[option.name]){
                var uri = this.actions[option.name].uri;
                var method = this.actions[option.name].method || "get";
                if (option.parameter){
                    Object.keys(option.parameter).forEach(function(key){
                        var v = option.parameter[key];
                        uri = uri.replace("{"+key+"}", v);
                    });
                }
                var res = null;
                try{
                    switch (method.toLowerCase()){
                        case "get":
                            res = restfulAcpplication.getQuery(this.root, uri);
                            break;
                        case "post":
                            res = restfulAcpplication.postQuery(this.root, uri, JSON.stringify(option.data));
                            break;
                        case "put":
                            res = restfulAcpplication.putQuery(this.root, uri, JSON.stringify(option.data));
                            break;
                        case "delete":
                            res = restfulAcpplication.deleteQuery(this.root, uri);
                            break;
                        default:
                            res = restfulAcpplication.getQuery(this.root, uri);
                    }
                    if (res){
                        var json = JSON.parse(res.toString());
                        if (option.success) option.success(json);
                    }else{
                        if (option.failure) option.failure();
                    }
                }catch(e){
                    if (option.failure) option.failure(e);
                }
            }
        }
    }
})();
Action.applications = this.applications;

var Actions = {
    'get': function(root){
        if (loadedActions[root]) return loadedActions[root];
        loadedActions[root] = {
            "root": root,
            "get": function(uri, success, failure){
                return returnRes(estfulAcpplication.getQuery(this.root, uri), success, failure);
            },
            "post": function(uri, data, success, failure){
                return returnRes(estfulAcpplication.postQuery(this.root, uri, JSON.stringify(data)), success, failure);
            },
            "put": function(uri, data, success, failure){
                return returnRes(estfulAcpplication.putQuery(this.root, uri, JSON.stringify(data)), success, failure);
            },
            "delete": function(uri, success, failure){
                return returnRes(estfulAcpplication.deleteQuery(this.root, uri), success, failure);
            }
        };
        return loadedActions[root];
    }
};

bind.library = library;
bind.data = this.data;
bind.workContext = wrapWorkContext;
bind.service = this.webservicesClient;
bind.org = org;
bind.Action = Action;
bind.Actions = Actions;
//bind.organization = this.organization;
bind.include = include;
bind.define = define;
bind.Dict = Dict;
bind.form = null;
bind.body = {
    "set": function(data){
        if ((typeof data)==="string"){
            body.set(data);
        }
        if ((typeof data)==="object"){
            body.set(JSON.encode(data));
        }
    }
};
bind.parameters = this.parameters || null;
bind.response = (function(){
    if (this.jaxrsResponse){
        if (this.jaxrsResponse.get()){
            if (JSON.validate(this.jaxrsResponse.get())){
                return {
                    "status": this.jaxrsResponse.status,
                    "value": JSON.decode(this.jaxrsResponse.get())
                };
            }else{
                return {
                    "status": this.jaxrsResponse.status,
                    "value": this.jaxrsResponse.value
                };
            }
        }else{
            return {"status": this.jaxrsResponse.status};
        }
    }
    return null;
}).apply(this);

bind.assginData = {
    "data": null,
    "get": function(){
        this.data = JSON.decode(assginData.get());
        return this.data;
    },
    "set": function(data){
        assginData.set(JSON.encode(data || this.data));
    }
};
bind.expire = {
    "setHour": function(hour){
        try{expire.setHour(hour);}catch(e){}
    },
    "setWorkHour": function(hour){
        try{expire.setWorkHour(hour);}catch(e){}
    },
    "setDate": function(date){
        try{expire.setDate(date);}catch(e){}
    }
};


var app = this.form.getApp();
var _form = app.appForm;

_form.readedWork = function(){
    var read = null;
    for (var i = 0; i < _form.businessData.readList.length; i++) {
        if (_form.businessData.readList[i].person === layout.session.user.distinguishedName) {
            read = _form.businessData.readList[i];
            break;
        }
    }
    app.action.setReaded(function () {
        if (layout.mobile) {
            _form.finishOnMobile();
        } else {
            app.reload();
        }
    }, null, read.id, read);
}
