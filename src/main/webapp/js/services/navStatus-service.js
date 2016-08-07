(function() {
	'use strict';

angular.module('adminConsoleApp').factory('navStatus', function() {
	var loading = true;
	var idle = false;
	var navStatus ={};
	var loadingCount=0;
	var errorList =  [];


	navStatus.startLoading = function() {
		loadingCount++;
		checkLoading();
	};

	navStatus.endLoading = function() {
		if(loadingCount > 0) {
			loadingCount--;
		}
		checkLoading();
	};

	function checkLoading() {
		loading = loadingCount > 0;
	}
	navStatus.reset = function() {
		loadingCount=0;
		loading=false;
		errorList = [];
	};
	navStatus.getLoadingCount = function() {
		return loadingCount;
	};
	navStatus.getLoading = function() {
		return loading;
	};

	navStatus.idle = function(currentIdleStatus) {
		idle = currentIdleStatus;
	};
	navStatus.getIdle = function() {
		return idle;
	};

	navStatus.addError = function(status,description,exceptionList) {
		if(status >= 400 && status < 600) {
			if(status == 404 && !description) {
				description = "404 Resource not found"
			} else if(status == 500 && !description) {
				description = "500 Server Error"
			} else if(status == 503 && !description) {
				description = "503 Service unavailable"
			}else if(!description) {
				description = "Status: "+status;
			}

			var isDouble = false;
			for(var i=0;i<errorList.length;i++) {
				if(errorList[i].status == status && errorList[i].description == description) {
					errorList[i].double++;
					errorList[i].date=new Date();
					isDouble = true;
					break;
				}
			}

			if(!isDouble) {
				errorList.unshift({
					id: "alertId_"+Math.floor((Math.random() * 999999999999)),
					status: status,
					description: description,
					exceptionList: exceptionList,
					date: new Date(),
					double: 1
				});
				console.log("New error: "+status+" / "+description+" / "+ exceptionList);
			}
		}
	};

	navStatus.removeError = function(id) {
		for(var i=0;i<errorList.length;i++) {
			if(errorList[i].id == id) {
				errorList.splice(i,1);
				break;
			}
		}
	};

	navStatus.getErrors = function() {
		return errorList;
	};

	return navStatus;
});

})();