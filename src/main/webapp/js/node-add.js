'use strict';

angular.module('adminConsoleApp').controller('NodeAddModalCtrl', ['$scope','$http','navStatus','$modalInstance', function($scope, $http, navStatus,$modalInstance) {
	$scope.createNewMode = true;
	$scope.roomObj = {};

	if($scope.wrapper == undefined || $scope.wrapper.node == undefined) {
		$scope.node = {};
		$scope.createNewMode=true;
	} else {
		$scope.node = $scope.wrapper.node;
		$scope.wrapper.etag = '';
		$scope.createNewMode=false;
	}

	if($scope.wrapper.roomList == undefined) {
		$scope.roomList={};
		$scope.roomList.rooms=[];
	} else {
		$scope.roomList = $scope.wrapper.roomList;

		if($scope.node.roomId) {
			for(var i=0;i<$scope.roomList.rooms.length;i++) {
				if($scope.roomList.rooms[i].roomId == $scope.node.roomId) {
					copyRoom($scope.roomList.rooms[i],$scope.roomObj);
					break;
				}
			}
		}
	}

	$scope.ping={};
	$scope.ping.http=$scope.ping.https=$scope.ping.httpCgi=$scope.ping.httpsCgi=null;

	$scope.save = function () {
		navStatus.startLoading();

		if(!$scope.roomObj.roomId && $scope.roomObj.name) { //create new room
			var selectedRoom = {};
			copyRoom($scope.roomObj,selectedRoom);
			$scope.roomList.rooms.push(selectedRoom);

			if($scope.roomList && $scope.roomList.networkId) {
				navStatus.startLoading();
				$http({
					method: 'PUT',
					url: constants.getApiUrl() + "/networkservice/" + $scope.networkId + "/roomlist",
					data: $scope.roomList
				}).success(function (data, status, headers) {
					navStatus.endLoading();
					$scope.roomList = data;
					for(var i=0;i<$scope.roomList.rooms.length;i++) {
						if($scope.roomList.rooms[i].name == $scope.roomObj.name) {
							copyRoom($scope.roomList.rooms[i],$scope.roomObj);
							break;
						}
					}
					saveNode();
				}).error(function (data, status, headers, config) {
					navStatus.endLoading();
				});
			} else {
				console.log("cannot save roomlist");
			}
		} else if($scope.roomObj.roomId && $scope.roomObj.name) { //room that exists was selected
			$scope.node.roomId = $scope.roomObj.roomId;
			saveNode();
		} else { //room was deleted
			$scope.node.roomId = undefined;
			saveNode();
		}
	};

	function copyRoom(master,copy) {
		copy.roomId = master.roomId;
		copy.name = master.name;
		copy.created = master.created;
	}

	function saveNode() {
		$http({
			method: $scope.createNewMode ? 'POST' : 'PUT',
			url: constants.getApiUrl() + "/network/"+$scope.networkId+"/node",
			data:$scope.node
		}).success(function () {
			navStatus.endLoading();
			$scope.$emit('updateSensorNodesAndNetwork');
			$modalInstance.close();
		}).error(function (data, status, headers, config) {navStatus.endLoading();});
	}

	$scope.roomSelect = function(item, model, label) {
		copyRoom(item,$scope.roomObj);
		console.log("selected: "+item.roomId+", "+model.roomId+", "+label)
	};

	$scope.roomTfChanged = function() {
		$scope.roomObj.roomId = undefined;
//		console.log("onchange "+$scope.roomObj.name);
	};

	$scope.discover = function() {
		ping(true,true);
		ping(false,true);
		routerInfo();
	};

	function ping (pingHttps,checkCgi) {
		$scope.localLoading=true;
		$scope.node.httpsEnabled = pingHttps;
		$scope.ping.http=$scope.ping.https=$scope.ping.httpCgi=$scope.ping.httpsCgi=null;

		$http({
			method: 'POST',
			params: {'persist':false},
			url:constants.getApiUrl() + "/sensor/"+(checkCgi ? "ping-cgi":"ping")+"/",
			data: $scope.node
		}).success(function (data) {
			setPingStatus(pingHttps,checkCgi,data.success);
		}).error(function (data, status, headers, config) {
			setPingStatus(pingHttps,checkCgi,false);});
	}

	function setPingStatus(https,cgi,status) {
		if(https) {
			if(cgi) {
				$scope.ping.httpCgi = status;
			} else {
				$scope.ping.http = status;
			}
		} else {
			if(cgi) {
				$scope.ping.httpsCgi = status;
			} else {
				$scope.ping.https = status;
			}
		}
	}

	function routerInfo() {
		$scope.localLoading=true;
		$http({
			method: 'POST',
			url:constants.getApiUrl() + "/sensor/info/",
			data: $scope.node
		}).success(function (data) {
			$scope.node.adapters = data.adapterList;
			$scope.node.machineInfo = data.machineInfo;
			$scope.localLoading=false;
		}).error(function (data, status, headers, config) {
			$scope.localLoading=false;
			$scope.node.adapters=[];
		});
	}

	$scope.deleteAdapter = function(macAddress) {
		if($scope.node.adapters != undefined) {
			for(var i=0;i<$scope.node.adapters.length;i++) {
				if ($scope.node.adapters[i].macAddress == macAddress) {
					$scope.node.adapters.splice(i, 1);
				}
			}
		}
	};

	$scope.close = function () {
		$scope.$emit('updateSensorNodesAndNetwork');
		$modalInstance.close();
	};
}]);