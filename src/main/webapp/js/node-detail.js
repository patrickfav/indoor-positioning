'use strict';

angular.module('adminConsoleApp').controller('NodeDetailCtrl', ['$scope', '$http', 'navStatus', '$routeParams', '$modal', function ($scope, $http, navStatus, $routeParams, $modal) {

	$scope.networkId = "";
	$scope.nodeId = "";
	$scope.wrapper = {};
	$scope.surveys = {};
	$scope.rebootButtonDisabled = false;
	$scope.sampleSize = 100;

	(function init() {
		console.log("init node details");
		$scope.networkId = $routeParams.networkId;
		$scope.nodeId = $routeParams.nodeId;
		navStatus.reset();
		reloadNode(true);
	})();

	function reloadNode(reloadSurveys) {
		navStatus.startLoading();
		$http({
			method: 'GET',
			url: constants.getApiUrl() + "/network/" + $scope.networkId + "/node/" + $scope.nodeId,
			headers: {'If-None-Match': $scope.wrapper.etag}})
			.success(function (data, status, headers, config) {
				$scope.wrapper.node = data.sensorNode;
				$scope.wrapper.network = data.sensorNetwork;
				$scope.wrapper.pingList = data.pingList;
				$scope.wrapper.roomList = data.roomList;
				$scope.wrapper.etag = headers("Etag");

				if (reloadSurveys) {
					for (var i = 0; i < $scope.wrapper.node.adapters.length; i++) {
						loadSurveys($scope.wrapper.node.adapters[i].name);
					}
				}
				navStatus.endLoading();
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
	}

	$scope.getRoomNameById = function (roomId) {
		if ($scope.wrapper.roomList && roomId) {
			for (var i = 0; i < $scope.wrapper.roomList.rooms.length; i++) {
				if ($scope.wrapper.roomList.rooms[i].roomId == roomId) {
					return $scope.wrapper.roomList.rooms[i].name;
				}
			}
		}
		return "unknown"
	};

	$scope.getFreqForAdapterName = function (adapterName) {
		if ($scope.wrapper.node) {
			for (var i = 0; $scope.wrapper.node.adapters.length; i++) {
				if ($scope.wrapper.node.adapters[i].name == adapterName) {
					return $scope.wrapper.node.adapters[i].frequencyRange;
				}
			}
		}
		return null;
	};

	$scope.setMultiplier = function () {
		console.log("update multiplier");
		saveNode();
	};

	function saveNode() {
		$http({
			method: 'PUT',
			url: constants.getApiUrl() + "/network/" + $scope.networkId + "/node",
			data: $scope.wrapper.node
		}).success(function () {
			navStatus.endLoading();
			reloadNode();
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status, data.statusDescription, data.exceptionList);
		});
	}

	function loadSurveys(adapter) {
		navStatus.startLoading();
		if ($scope.surveys[adapter] != undefined) {
			$scope.surveys[adapter].surveyChartConfig.loading = true;
		} else {
			$scope.surveys[adapter] = {};
			$scope.surveys[adapter].surveyChartConfig = {};
			$scope.surveys[adapter].surveyChartConfig.title = {text: ''};
			$scope.surveys[adapter].surveyChartConfig.loading = true;
		}
		$http({
			method: 'GET',
			params: {adapter: adapter, limit: $scope.sampleSize, nodeId: $scope.nodeId},
			url: constants.getApiUrl() + "/survey/",
			headers: {'If-None-Match': $scope.surveys[adapter].etag}
		}).success(function (data, status, headers, config) {
			$scope.surveys[adapter].etag = headers("Etag");
			$scope.surveys[adapter].list = data.surveyList;
			$scope.surveys[adapter].stats = data.statistics;
			$scope.surveys[adapter].frequencyChartConfig = undefined;
			$scope.surveys[adapter].surveyChartConfig = {
				title: {text: ''},
				subtitle: {text: ''},
				options: {
					chart: {type: 'scatter', zoomType: 'x'},
					tooltip: {
						crosshairs: true
					},
					credits: {"enabled": false},
					colors: constants.getFlatColors(),
					plotOptions: {
						scatter: {
							marker: {
								radius: 6,
								states: {
									hover: {
										enabled: true,
										lineColor: 'rgb(100,100,100)'
									}
								}
							},
							states: {
								hover: {marker: {enabled: false}}
							},
							tooltip: {
								headerFormat: '<b>{series.name}</b><br>',
								pointFormat: '<em>{point.x:%d.%m %H:%M:%S}</em> <strong>{point.y:.0f} dBm</strong>'
							}
						}
					}
				},
				yAxis: {
					title: {text: 'Signal Strength'},
					labels: {formatter: function () {
						return "-" + this.value.toString() + "dBm";
					}},
					plotBands: [
						{color: {
							linearGradient: { x1: 0, x2: 0, y1: 0, y2: 1 },
							stops: [
								[0, 'rgba(192,57,43,0.01)'],
								[1, 'rgba(192,57,43,0.2)']
							]
						}, from: -100, to: -80}
					]
				},
				xAxis: {
					type: 'datetime',
					dateTimeLabelFormats: {
						month: '%e. %b',
						year: '%b'
					},
					title: {
						text: 'Date'
					}
				},
				loading: false,
				series: []
			};

			var macMap = {};
			for (var i = 0; i < data.surveyList.length; i++) {
				for (var k = 0; k < data.surveyList[i].averageScanNodes.length; k++) {
					if (data.surveyList[i].averageScanNodes[k].ssid != undefined) {
						if (macMap[data.surveyList[i].averageScanNodes[k].ssid] == undefined) {
							macMap[data.surveyList[i].averageScanNodes[k].macAddress] = data.surveyList[i].averageScanNodes[k].ssid;
						}
					}
				}
			}

			var colorIndex = 0;
			for (var mac in macMap) {
				$scope.surveys[adapter].surveyChartConfig.series.push({
					id: mac + '_' + macMap[mac],
					name: macMap[mac].length == 0 ? '<empty>' : macMap[mac],
					ssid: macMap[mac],
					mac: mac,
					color: constants.getFlatColors()[colorIndex].replace("1.0", "0.5"),
					data: getDataSeriesFromSurveyList(data.surveyList, mac, macMap[mac])

				});

				colorIndex++;
				if (colorIndex >= constants.getFlatColors().length) {
					colorIndex = 0;
				}
			}
			navStatus.endLoading();
		}).error(function (data, status, headers, config) {
			$scope.surveys[adapter].surveyChartConfig.loading = false;
			navStatus.endLoading();
			navStatus.addError(status, data.statusDescription, data.exceptionList);
		});
	}


	$scope.setSampleSize = function (sampleSize) {
		$scope.sampleSize = sampleSize;

		for (var adapter in $scope.surveys) {
			$scope.surveys[adapter].etag = undefined;
		}
		for (var j = 0; j < $scope.wrapper.node.adapters.length; j++) {
			$scope.resetHighlight($scope.wrapper.node.adapters[j].name);
		}

		for (j = 0; j < $scope.wrapper.node.adapters.length; j++) {
			loadSurveys($scope.wrapper.node.adapters[j].name);
		}
	};

	function getDataSeriesFromSurveyList(surveyList, mac, ssid) {
		var data = [];
		for (var i = 0; i < surveyList.length; i++) {
			for (var k = 0; k < surveyList[i].averageScanNodes.length; k++) {
				if (ssid == surveyList[i].averageScanNodes[k].ssid && mac == surveyList[i].averageScanNodes[k].macAddress) {
					var datatuple = [];
					datatuple[0] = surveyList[i].averageScanNodes[k].date;
					datatuple[1] = surveyList[i].averageScanNodes[k].statistics.mean;
					data.push(datatuple);
				}
			}
		}
		return data;
	}

	$scope.highlightLine = function (ssid, mac, adapter) {
		if ($scope.surveys[adapter] != undefined) {
			$scope.surveys[adapter].highlight = {};
			$scope.surveys[adapter].highlight.highlighted = true;
			$scope.surveys[adapter].highlight.ssid = ssid;
			$scope.surveys[adapter].highlight.mac = mac;

			$scope.surveys[adapter].surveyChartConfig.title.text = ssid + ' (' + mac + ')';

			for (var i = 0; i < $scope.surveys[adapter].surveyChartConfig.series.length; i++) {
				if ($scope.surveys[adapter].surveyChartConfig.series[i].ssid == ssid && $scope.surveys[adapter].surveyChartConfig.series[i].mac == mac) {
					$scope.surveys[adapter].surveyChartConfig.series[i].visible = true;

					for (var k = 0; k < $scope.surveys[adapter].stats.length; k++) {
						for (var key in $scope.surveys[adapter].stats[k].ssidSet) {
							if (ssid == $scope.surveys[adapter].stats[k].ssidSet[key] && mac == $scope.surveys[adapter].stats[k].macAddress) {
								var stdErr = $scope.surveys[adapter].stats[k].statistics.sdtErr60Interval;
								var stdErr2 = $scope.surveys[adapter].stats[k].statistics.sdtErr80Interval;
								var avg = $scope.surveys[adapter].stats[k].statistics.mean;
								$scope.surveys[adapter].surveyChartConfig.options.labels = {items: [
									{
										html: '<strong>60% Confidence Intervall: +/-' + stdErr.toFixed(2) + '</strong><br/><strong>80% Confidence Intervall: +/-' + stdErr2.toFixed(2) + '</strong>',
										style: {left: '110px', top: '5px', color: 'rgba(0,0,0,0.6)', fontSize: '12px'}
									}
								]};
								$scope.surveys[adapter].surveyChartConfig.yAxis.plotBands[1] = {
									color: 'rgba(45,137,239,0.2)', from: avg - stdErr, to: avg + stdErr};
								$scope.surveys[adapter].surveyChartConfig.yAxis.plotBands[2] = {
									color: 'rgba(255,196,13,0.1)', from: avg - stdErr2, to: avg + stdErr2};
								$scope.surveys[adapter].surveyChartConfig.yAxis.plotLines = [
									{
										color: '#00a300',
										width: 2,
										value: avg,
										dashStyle: 'Dash'
									}
								];
							}
						}
					}
				} else {
					$scope.surveys[adapter].surveyChartConfig.series[i].visible = false;
				}

			}
			$scope.surveys[adapter].surveyChartConfig.series.push(createRegressionSeries(getDataSeriesFromSurveyList($scope.surveys[adapter].list, mac, ssid)));
			createFrequencyGraph(adapter, ssid, mac);
		}
	};

	function createRegressionSeries(dataArray) {
		var regressionSeries = {};
		regressionSeries.data = regression('linear', dataArray).points;
		regressionSeries.name = "Trend";
		regressionSeries.id = "regression";
		regressionSeries.visible = true;
		regressionSeries.type = "spline";
		regressionSeries.color = '#b91d47';
		regressionSeries.marker = {enabled: false};
		regressionSeries.tooltip = {
			headerFormat: '<b>{point.x:%d.%m %H:%M:%S}</b><br>',
			pointFormat: '<strong>{point.y:.0f} dBm</strong>'
		};
		return regressionSeries;
	}

	$scope.resetHighlight = function (adapter) {
		if ($scope.surveys[adapter] != undefined && $scope.surveys[adapter].highlight != undefined) {
			$scope.surveys[adapter].highlight.highlighted = false;
			$scope.surveys[adapter].highlight.ssid = $scope.surveys[adapter].highlight.mac = '';
			$scope.surveys[adapter].surveyChartConfig.title.text = '';

			for (var i = 0; i < $scope.surveys[adapter].surveyChartConfig.series.length; i++) {
				$scope.surveys[adapter].surveyChartConfig.series[i].visible = true;
				$scope.surveys[adapter].surveyChartConfig.options.labels = {};
				$scope.surveys[adapter].surveyChartConfig.yAxis.plotBands[1] = {};
				$scope.surveys[adapter].surveyChartConfig.yAxis.plotBands[2] = {};

				if ($scope.surveys[adapter].surveyChartConfig.series[i].id == "regression") {
					$scope.surveys[adapter].surveyChartConfig.series.splice(i, 1);
				}
			}
		}
		removeFrequencyGraph(adapter);
	};

	$scope.getGraphColor = function (ssid, mac, adapter) {
		if ($scope.surveys[adapter] != undefined) {
			for (var i = 0; i < $scope.surveys[adapter].surveyChartConfig.series.length; i++) {
				if ($scope.surveys[adapter].surveyChartConfig.series[i].ssid == ssid && $scope.surveys[adapter].surveyChartConfig.series[i].mac == mac) {
					return $scope.surveys[adapter].surveyChartConfig.series[i].color.replace("0.5", "1.0");
				}
			}
		}
	};


	$scope.refreshSurvey = function () {
		if ($scope.wrapper.node != undefined) {
			for (var i = 0; i < $scope.wrapper.node.adapters.length; i++) {
				loadSurveys($scope.wrapper.node.adapters[i].name);
			}
		} else {
			reloadNode();
		}
	};

	function createFrequencyGraph(adapter, ssid, mac) {
		if ($scope.surveys[adapter] != undefined) {
			$scope.surveys[adapter].frequencyChartConfig = {
				title: {text: ''},
				options: {
					credits: {"enabled": false},
					colors: ['#2d89ef'],
					tooltip: {pointFormat: 'Frequency: <strong>{point.y}</strong>'},
					plotOptions: {series: {marker: {enabled: false}}}
				},
				yAxis: {
					title: {text: 'Frequency'}
				},
				xAxis: {
					type: 'category',
					title: {text: 'Signal Strength (dBm)'}
				},
				legend: {enabled: false},

				series: []
			};
			for (var k = 0; k < $scope.surveys[adapter].stats.length; k++) {
				for (var key in $scope.surveys[adapter].stats[k].ssidSet) {
					if (ssid == $scope.surveys[adapter].stats[k].ssidSet[key] && mac == $scope.surveys[adapter].stats[k].macAddress) {

						var serie = {};
						serie.name = "Frequency Distribution";
						serie.type = "column";
						serie.data = [];

						var serieGauss = {};
						serieGauss.name = "Normal Distribution";
						serieGauss.type = "spline";
						serieGauss.color = '#b91d47';
						serieGauss.data = [];

						var serieLogGauss = {};
						serieLogGauss.name = "Log Normal Distribution";
						serieLogGauss.type = "spline";
						serieLogGauss.color = '#b9871d';
						serieLogGauss.data = [];
						serieLogGauss.visible = false;

						var freqValue = $scope.surveys[adapter].stats[k].statistics.min;
						for (var h = 0; h < $scope.surveys[adapter].stats[k].statistics.frequencyDistribution.length; h++) {
							var name = freqValue + '';

							serie.data.push([name, $scope.surveys[adapter].stats[k].statistics.frequencyDistribution[h]]);
							var yNormalDistribution = gaussFunc(freqValue, $scope.surveys[adapter].stats[k].statistics.mean, $scope.surveys[adapter].stats[k].statistics.stdDev) * $scope.surveys[adapter].stats[k].statistics.dataSize;
							serieGauss.data.push([name, yNormalDistribution]);

							var yLogNormalDistribution = logGaussFunc(freqValue, $scope.surveys[adapter].stats[k].statistics.mean, $scope.surveys[adapter].stats[k].statistics.stdDev, $scope.surveys[adapter].stats[k].statistics.min) * $scope.surveys[adapter].stats[k].statistics.dataSize;
							serieLogGauss.data.push([name, yLogNormalDistribution]);

							freqValue++;
						}

						$scope.surveys[adapter].frequencyChartConfig.series.push(serieGauss);
						$scope.surveys[adapter].frequencyChartConfig.series.push(serieLogGauss);
						$scope.surveys[adapter].frequencyChartConfig.series.push(serie);


						var plotLineMean = Math.abs($scope.surveys[adapter].stats[k].statistics.min) - Math.abs($scope.surveys[adapter].stats[k].statistics.mean);
						var plotLineMedian = Math.abs($scope.surveys[adapter].stats[k].statistics.min) - Math.abs($scope.surveys[adapter].stats[k].statistics.median);
						var plotLineMode = Math.abs($scope.surveys[adapter].stats[k].statistics.min) - Math.abs($scope.surveys[adapter].stats[k].statistics.mode);

						$scope.surveys[adapter].frequencyChartConfig.xAxis.plotLines = [
							{color: '#00a300', width: 2, value: plotLineMean, label: {text: 'Mean'}, zIndex: 3},
							{color: '#e3a21a', width: 2, value: plotLineMedian, label: {text: 'Median'}, zIndex: 3},
							{color: '#603cba', width: 2, value: plotLineMode, label: {text: 'Mode'}, zIndex: 3}
						];


						$scope.surveys[adapter].frequencyChartConfig.options.labels = {items: [
							{
								html: '<strong>μ=' + $scope.surveys[adapter].stats[k].statistics.mean.toFixed(2) + '</strong><br/>' +
									'<strong>σ²=' + $scope.surveys[adapter].stats[k].statistics.variance.toFixed(2) + '</strong><br/>' +
									'<strong>skew=' + $scope.surveys[adapter].stats[k].statistics.skewness.toFixed(2) + '</strong><br/>' +
									'<strong>kurtosis=' + $scope.surveys[adapter].stats[k].statistics.kurtosis.toFixed(2) + '</strong>',
								style: {left: '60px', top: '10px', color: '#1b2630', fontSize: '14px'}
							}
						]};
						break;
					}
				}
			}
		}
	}

	function removeFrequencyGraph(adapter) {
		$scope.surveys[adapter].frequencyChartConfig = undefined;
	}

	function gaussFunc(x, mean, stdDev) {
		var a = 1 / (stdDev * 2.506628274631);
		var b = mean;
		var c = stdDev;
		var d = 0;

		return a * Math.exp(-(Math.pow((x - b), 2) / (2 * Math.pow(c, 2)))) + d
	}

	function logGaussFunc(x, mean, stdDev, offset) {
		var offsetMean = Math.abs(offset) - Math.abs(mean);
		mean = Math.abs(Math.log(Math.pow(offsetMean, 2) / Math.sqrt(Math.pow(offsetMean, 2) + Math.pow(stdDev, 2))));
		stdDev = Math.sqrt(Math.log(1 + (Math.pow(stdDev, 2) / Math.pow(mean, 2))));
		var offsetX = Math.abs(offset) - Math.abs(x);
		var result = (1 / (offsetX * stdDev * Math.sqrt(2 * Math.PI))) * Math.exp(-(Math.pow(Math.log(offsetX) - mean, 2) / (2 * Math.pow(stdDev, 2))));
		return isNaN(result) ? 0 : result;
	}

	$scope.switchEnabled = function () {
		if ($scope.wrapper.node.enabled == undefined) {
			$scope.wrapper.node.enabled = true;
		}
		$scope.wrapper.node.enabled = !$scope.wrapper.node.enabled;
		updateNode();
	};

	function updateNode() {
		navStatus.startLoading();
		$http.put(constants.getApiUrl() + "/network/" + $scope.networkId + "/node/", $scope.wrapper.node)
			.success(function (data) {
				reloadNode(false);
				navStatus.endLoading();
			}).error(function (data, status, headers, config) {
				navStatus.endLoading();
				navStatus.addError(status, data.statusDescription, data.exceptionList);
			});
	}

	function reboot() {
		navStatus.startLoading();
		$http.post(constants.getApiUrl() + "/sensor/reboot", $scope.node).success(function (data) {
			$scope.rebootButtonDisabled = true;
			navStatus.endLoading();
		}).error(function (data, status, headers, config) {
			navStatus.endLoading();
			navStatus.addError(status, data.statusDescription, data.exceptionList);
		});

	}

	$scope.getExportArray = function () {
		var array = [];
		if ($scope.surveys) {
			for (var adapterName in $scope.surveys) {
				for(var i = 0;i<$scope.surveys[adapterName].stats.length;i++) {
					var csvObj = {};
					csvObj.macAddress = $scope.surveys[adapterName].stats[i].macAddress;

					csvObj.macAddress = $scope.surveys[adapterName].stats[i].macAddress;
					csvObj.frequencyRange = $scope.surveys[adapterName].stats[i].frequencyRange;
					csvObj.ssidSet = $scope.surveys[adapterName].stats[i].ssidSet;
					csvObj.mean = $scope.surveys[adapterName].stats[i].statistics.mean;
					csvObj.median = $scope.surveys[adapterName].stats[i].statistics.median;
					csvObj.max = $scope.surveys[adapterName].stats[i].statistics.max;
					csvObj.min = $scope.surveys[adapterName].stats[i].statistics.min;
					csvObj.mode = $scope.surveys[adapterName].stats[i].statistics.mode;
					csvObj.stdDev = $scope.surveys[adapterName].stats[i].statistics.stdDev;
					csvObj.variance = $scope.surveys[adapterName].stats[i].statistics.variance;
					csvObj.dataSize = $scope.surveys[adapterName].stats[i].statistics.dataSize;
					csvObj.sdtErr60Interval = $scope.surveys[adapterName].stats[i].statistics.sdtErr60Interval;
					csvObj.sdtErr80Interval = $scope.surveys[adapterName].stats[i].statistics.sdtErr80Interval;
					csvObj.sdtErr90Interval = $scope.surveys[adapterName].stats[i].statistics.sdtErr90Interval;
					csvObj.nodeName = $scope.wrapper.node.nodeName;
					array.push(csvObj);
				}
			}
		}
		return array;
	};

	$scope.$on('updateSensorNodes', function (event, data) {
		reloadNode(true);
	});

	$scope.openUpdateNodeModal = function () {
		var modalInstance = $modal.open({
			templateUrl: 'partials/modal/node_add.html',
			controller: 'NodeAddModalCtrl',
			backdrop: 'static',
			size: 'lg',
			scope: $scope
		});
	};
}]);
