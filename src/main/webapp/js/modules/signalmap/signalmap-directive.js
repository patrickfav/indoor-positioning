(function() {
	'use strict';

	angular.module('signalmap-ng', []).directive('snSignalMapCanvas',['signalmapState','signalmapUtils','signalmapUpdateAndTransform','signalmapInit','signalmapMousemanager','signalmapCallbacks', function (state,utils,update,init,mousemanager,callbacks) {
		var divId = 'signal-map-'+Math.floor((Math.random() * 999999999999))+'-'+Math.floor((Math.random() * 999999999999))+'-'+Math.floor((Math.random() * 999999999999));
		return {
			restrict: 'E',

			template: '<canvas id="'+divId+'"></canvas>',
			scope: {
				signalMap: '=',
				freq: '=',
				pixelPerCm: '='
			},
			link: function postLink(scope, element, attrs) {

                element.on('mousedown', mousemanager.onMouseDown).on('mouseup', mousemanager.onMouseUp).on('mousemove', mousemanager.onMouseMove).on('click',mousemanager.onClick);

                initPaper();

                function initPaper() {
                    console.log("signalmap: setup paperjs");

                    paper.install(window);
                    paper.setup(divId);

                    init.redrawCanvas(paper,scope);


					element.on('$destroy', function() {
						console.log("signalmap: destroy");
						state.reset();
						callbacks.reset();
						paper.project.clear();
					});
                }

				scope.$watch('signalMap', function(newVal,oldVal) {
					console.log("refresh signalmap (watcher)");
					init.redrawCanvas(paper,scope);
				});
			}
		};
	}]);

})();