(function() {
	'use strict';

angular.module('sensor-ng', []).directive('snColoredDbm', function() {
	return {
		restrict: 'E',
		template: '<span ng-if="dbm" class="dB" ng-class="{\'text-success\':dbm >= -50 && dbm < -5,\'text-warning\':dbm >= -90 && dbm < -50,\'text-danger\':dbm < -90}">' +
			'{{dbm | number:fractionCount}}dBm</span>',
		scope: {
			dbm: '=',
			fractionCount :'@'
		}
	}
}).directive('snTrendInfo', function() {
	return {
		restrict: 'E',
		template: '<span tooltip-html-unsafe="Compared to last: {{trendInfo.shortTermTrend|number:2}}<br/>Compared to last {{trendInfo.longTermTrendSampleSize}}: {{trendInfo.longTermTrend|number:2}}"' +
			'class="glyphicon" ng-class="{' +
			'\'glyphicon-chevron-up text-success\':trendInfo.longTermTrendSampleSize > 0 && trendInfo[useShortTermValue?\'shortTermTrend\':\'longTermTrend\'] >= 1.5 && trendInfo[useShortTermValue?\'shortTermTrend\':\'longTermTrend\'] <= 5,' +
			'\'glyphicon-chevron-down text-danger\':trendInfo.longTermTrendSampleSize > 0 && trendInfo[useShortTermValue?\'shortTermTrend\':\'longTermTrend\'] <= -1.5 && trendInfo[useShortTermValue?\'shortTermTrend\':\'longTermTrend\'] >= -5,' +
			'\'glyphicon-arrow-up text-success\':trendInfo.longTermTrendSampleSize > 0 && trendInfo[useShortTermValue?\'shortTermTrend\':\'longTermTrend\'] > 5,'+
			'\'glyphicon-arrow-down text-danger\':trendInfo.longTermTrendSampleSize > 0 && trendInfo[useShortTermValue?\'shortTermTrend\':\'longTermTrend\'] < -5,'+
			'\'glyphicon-asterisk\': trendInfo.longTermTrendSampleSize == 0}"></span>',
		scope: {
			trendInfo: '=',
			useShortTermValue:'='
		}
	}
}).directive('ngEnter', function () {
	return function (scope, element, attrs) {
		element.bind("keydown keypress", function (event) {
			if(event.which === 13) {
				scope.$apply(function (){
					scope.$eval(attrs.ngEnter);
				});

				event.preventDefault();
			}
		});
	};
}).directive('snLoadingBtn', function () {
	return {
		restrict: 'A',
		scope: {
			loadingState :'='
		},
		link: function (scope, element, attrs) {
			var btnContent = element.html();
			var elemWidth = element.prop('offsetWidth');

			scope.$watch('loadingState', function(newValIsLoading,oldVal) {
				if(newValIsLoading === true) {
					btnContent = element.html();
//					element.html('<span style="width:'+elemWidth+'px" class="glyphicon glyphicon-refresh"></span>');
					element.attr('disabled','disabled');
//					element.addClass('opacityPulse');
					element.addClass('loadingBtn');
				} else {
//					element.html(btnContent);
					element.removeAttr("disabled");
//					element.removeClass('opacityPulse');
					element.removeClass('loadingBtn');
				}
			});
		}
	}
}).directive('snRelativeTarget', function() {
	return {
		restrict: 'E',
		template: '<span ng-if="currentDistanceM" ng-class="{\'text-success\':offset != 0 && offset <= 0.5 && offset >= -0.5,\'text-warning\':(offset <= 1.5 && offset > 0.5) || (offset >= -1.5  && offset < -0.5),\'text-danger\':offset > 1.5 || offset < -1.5}">' +
			'<span ng-show="offset > 0.5 || offset < -0.5" tooltip="Target: {{targetDistanceM}}m" class="glyphicon" ng-class="{\'glyphicon glyphicon-arrow-up\':offset > 0.5,\'glyphicon glyphicon-arrow-down\':offset < -0.5}"></span>{{currentDistanceM | number:2}}m</span>',
		scope: {
			currentDistanceM: '=',
			targetDistanceM:'='
		},
		link: function (scope, element, attrs) {
			scope.offset = 0;

			scope.$watch('currentDistanceM', function(newVal,oldVal) {
				updateOffset(newVal,scope.targetDistanceM)
			});
			scope.$watch('targetDistanceM', function(newVal,oldVal) {
				updateOffset(scope.currentDistanceM,newVal)
			});

			function updateOffset(current,target) {
				if(current != undefined && target != undefined) {
					scope.offset =current-target;
				} else if(target == undefined) {
					scope.offset = 0;
				}
			}
		}
	}
})
})();