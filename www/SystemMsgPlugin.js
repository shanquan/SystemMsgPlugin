var exec = require('cordova/exec');

module.exports = {
    checkState: function(success, error, msmFlag, phoneFlag) { // only android
        exec(success, error, "SystemMsg", "checkState", [msmFlag, phoneFlag]);
    },
    listenMsm: function(success, error) { // only android
        exec(success, error, "SystemMsg", "listenMsm", []);
    },
    listNotification: function(success, error, selectPackages) { // only android
        exec(success, error, "SystemMsg", "listNotification", [selectPackages]);
    },
    listenCall: function(success, error) {
        exec(success, error, "SystemMsg", "listenCall", []);
    },
    cancelListenMsg: function(success, error) { // only android
        exec(success, error, "SystemMsg", "cancelListenMsg", []);
    },
    cancelNotification: function(success, error) { // only android
        exec(success, error, "SystemMsg", "cancelNotification", []);
    },
    cancelListenCall: function(success, error) {
        exec(success, error, "SystemMsg", "cancelListenCall", []);
    },
    jumpToNotifiActivity: function(success, error, selectPackages, notificationFlag) { // only android
        exec(success, error, "SystemMsg", "jumpToNotifiActivity", [selectPackages, notificationFlag]);
    }
};