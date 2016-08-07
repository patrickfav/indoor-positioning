'use strict';

angular.module('adminConsoleApp').controller('NetworkAddModalCtrl', ['$scope', '$http', 'navStatus', '$window', '$timeout', '$modalInstance', '$upload', function ($scope, $http, navStatus, $window, $timeout, $modalInstance, $upload) {
	$scope.createNewMode = true;
	var newFile = false;

	$scope.rndCronScheduleMin = function rndCronScheduleMin(everyXMin) {
		return    Math.floor((Math.random() * 60)) + ' 0/' + everyXMin + ' * 1/1 * ? *'
	};
	$scope.rndCronScheduleHour = function rndCronScheduleHour(everyXHour) {
		return    Math.floor((Math.random() * 60)) + ' ' + Math.floor((Math.random() * 60)) + ' 0/' + everyXHour + ' 1/1 * ? *'
	};

	if ($scope.wrapper == undefined || $scope.wrapper.network == undefined) {
		$scope.network = {};
		$scope.createNewMode = true;
		$scope.cronSurveyTitle = 'Every 15 Minutes';
		$scope.cronPingTitle = 'Every 10 Minutes';
		$scope.cronAnalysisTitle = 'Every 6 Hours';
		$scope.network.cronScheduleSurvey = $scope.rndCronScheduleMin(15);
		$scope.network.cronSchedulePing = $scope.rndCronScheduleMin(10);
		$scope.network.cronScheduleAnalysis = $scope.rndCronScheduleHour(6);
	} else {
		$scope.network = $scope.wrapper.network;
		$scope.wrapper.etag = '';
		$scope.createNewMode = false;

		for(var attachmentName in $scope.network._attachments) {
			if(attachmentName.indexOf("blueprint") > -1) {
				$scope.fileToUpload = {};
				$scope.fileToUpload.name = attachmentName;
				$scope.fileToUpload.size = $scope.network._attachments[attachmentName].length;
				$scope.fileToUpload.dataUrl = $scope.serverConfig.couchDBUrl+"/"+$scope.network._id+"/"+attachmentName+"?cache-control-rev="+$scope.network._attachments[attachmentName].revpos;
			}
		}
	}

	$scope.save = function () {
		save();
	};

	function saveNetwork() {
		navStatus.startLoading();
		$http({
			method: $scope.createNewMode ? 'POST' : 'PUT',
			url: constants.getApiUrl() + "/network",
			data: $scope.network
		}).success(function () {
			navStatus.endLoading();
			$scope.$emit('updateSensorNodesAndNetwork');
			$modalInstance.close();
		}).error(function (data, status, headers, config) {
			navStatus.addError(status, data.statusDescription, data.exceptionList);
			navStatus.endLoading();
		});
	}

	function save() {
		navStatus.startLoading();
		if(newFile && $scope.fileToUpload && !$scope.createNewMode) {
			$scope.upload = $upload.upload({
				url: constants.getApiUrl() + "/network/" + $scope.network.networkId + "/upload-blueprint", //upload.php script, node.js route, or servlet url
				method: 'POST',
				params:{'content-type':$scope.fileToUpload.type},
				file: $scope.fileToUpload
			}).success(function (data, status, headers, config) {
				$scope.network = data;
				navStatus.endLoading();
				saveNetwork();
			}).progress(function (evt) {
				$scope.fileToUpload.progress = parseInt(100.0 * evt.loaded / evt.total);
			}).error(function (data, status, headers, config) {
				navStatus.addError(status, data.statusDescription, data.exceptionList);
				navStatus.endLoading();
			});
		} else {
			saveNetwork()
		}
	}

	$scope.deleteNetwork = function () {
		if (confirm("Do you really want to delete this Network?")) {
			navStatus.startLoading();
			$http({
				method: 'DELETE',
				url: constants.getApiUrl() + "/network/" + $scope.network.networkId
			}).success(function () {
				navStatus.endLoading();
				$window.location.replace("#/networks");

			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
			});
		}
	};

	$scope.setSurveySchedule = function (label, everyX, isMin) {
		$scope.cronSurveyTitle = label;
		$scope.network.cronScheduleSurvey = isMin ? $scope.rndCronScheduleMin(everyX) : $scope.rndCronScheduleHour(everyX);
	};

	$scope.setPingSchedule = function (label, everyX, isMin) {
		$scope.cronPingTitle = label;
		$scope.network.cronSchedulePing = isMin ? $scope.rndCronScheduleMin(everyX) : $scope.rndCronScheduleHour(everyX);
	};

	$scope.setAnalysisSchedule = function (label, everyX, isMin) {
		$scope.cronAnalysisTitle = label;
		$scope.network.cronScheduleAnalysis = isMin ? $scope.rndCronScheduleMin(everyX) : $scope.rndCronScheduleHour(everyX);
	};

	$scope.close = function () {
		$scope.$emit('updateSensorNodesAndNetwork');
		$modalInstance.close();
	};

	$scope.clearChosenFile = function () {
		$scope.fileToUpload=undefined;
		newFile = false;
		if($scope.network._attachments) {
			for (var attachmentName in $scope.network._attachments) {
				if (attachmentName.indexOf("blueprint") > -1) {
					delete $scope.network._attachments[attachmentName];
				}
			}
		}
	};

	$scope.onFileSelect = function ($files) {
		if($files.length > 1) {
			showError("Only choose one file");
		} else if($files[0].type.indexOf("image") == -1) {
			showError("Only image files allowed (e.g. png, jpg, svg)");
		} else {
			$scope.uploadErrorMsg='';
			$scope.fileToUpload = $files[0];
			$scope.fileToUpload.progress = 0;

			if (window.FileReader != null && (window.FileAPI == null || FileAPI.html5 != false) && $scope.fileToUpload.type.indexOf('image') > -1) {
				console.log("filereader supported");
				var fileReader = new FileReader();
				fileReader.readAsDataURL($scope.fileToUpload);
				var loadFile = function (fileReader) {
					fileReader.onload = function (e) {
						$timeout(function () {
							$scope.fileToUpload.dataUrl = e.target.result;
						});
					}
				}(fileReader);
			}
			newFile = true;
		}
	};

	function showError(msg) {
		$scope.uploadErrorMsg =msg;
	}

}]);