var exec = require('cordova/exec');

exports.initService = function(arg0, success, error) {
    exec(success, error, "EgoNotificacao", "initService", [arg0]);
};

exports.ativar = function(arg0, success, error){
    exec(success, error, "EgoNotificacao", "ativar", [arg0]);
};

exports.desativar = function(arg0, success, error){
    exec(success, error, "EgoNotificacao", "desativar", []);
};

exports.sincronizar = function(arg0, arg1, arg2, success, error){
	exec(success, error, "EgoNotificacao", "sincronizar", [arg0, arg1, arg2]);
};
