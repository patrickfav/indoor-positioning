(function() {
    'use strict';

    angular.module('signalmap-ng').factory('signalmapCallbacks', function() {
        var callbacks = {};

        var mouseMoveCallback;
        var viewSizeChangedCallback;
        var floorplanSizeChangedCallback;
        var floorplanScaleChangedCallback;
		var onCreateCallback;
	    var actualPosCallback;

        callbacks.setGridMoveCallback = function (callback) {
            mouseMoveCallback = callback;
        };
        callbacks.setViewSizeChangedCallback = function (callback) {
            viewSizeChangedCallback = callback;
        };
        callbacks.setFloorplanSizeChangedCallback = function (callback) {
            floorplanSizeChangedCallback = callback;
        };
        callbacks.setFloorplanScaleChangedCallback = function (callback) {
            floorplanScaleChangedCallback = callback;
        };

        callbacks.viewSizeChanged = function (viewSize) {
            if(viewSizeChangedCallback != undefined) {
                viewSizeChangedCallback(viewSize);
            }
        };
        callbacks.mouseMoveChanged = function (x,y) {
            if(mouseMoveCallback != undefined) {
                mouseMoveCallback(x,y);
            }
        };
        callbacks.floorplanSizeChanged = function (size) {
            if(floorplanSizeChangedCallback != undefined) {
                floorplanSizeChangedCallback(size);
            }
        };
        callbacks.floorplanScaleChanged = function (scale,freq) {
            if(floorplanScaleChangedCallback != undefined) {
                floorplanScaleChangedCallback(scale,freq);
            }
        };

		callbacks.reset = function() {
			mouseMoveCallback = undefined;
			viewSizeChangedCallback = undefined;
			floorplanSizeChangedCallback = undefined;
			floorplanScaleChangedCallback = undefined;
		};

	    callbacks.setOnCreateCallback = function(callback) {
		    onCreateCallback = callback;
	    };

	    callbacks.onCreate = function() {
		    if(onCreateCallback != undefined) {
			    onCreateCallback();
		    }
	    };

	    callbacks.setActualPosCallback = function(callback) {
		    actualPosCallback = callback;
	    };

	    callbacks.onActualPosCallback = function(actualPos) {
		    if(actualPosCallback != undefined) {
			    actualPosCallback(actualPos);
		    }
	    };

        return callbacks;
    });
})();