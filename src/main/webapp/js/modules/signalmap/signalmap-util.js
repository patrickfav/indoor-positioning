(function() {
	'use strict';

	angular.module('signalmap-ng').factory('signalmapUtils',function() {
		var utils = {};

		utils.getPointWithMousePosOffset= function (delta,itemPosition,paper) {
			var pos = new paper.Point();
			pos.x = itemPosition.x + delta.x;
			pos.y = itemPosition.y + delta.y;
			return pos;
		};

		utils.getSnapPosition = function (point,tileLenPx) {
			if(point) {
				var modX,modY;
				point.x = Math.round(point.x);
				point.y = Math.round(point.y);

				if((modX = point.x%tileLenPx) != 0) {
					if(modX > tileLenPx/2) {
						point.x = point.x - modX + tileLenPx;
					} else {
						point.x -= modX;
					}
				}
				if((modY = point.y%tileLenPx) != 0) {
					if(modY > tileLenPx/2) {
						point.y = point.y - modY + tileLenPx;
					} else {
						point.y -= modY;
					}
				}
			}
			return point;
		};

		utils.getTextAngle = function (line) {
			var edgeVector = line.lastSegment.point.subtract(line.firstSegment.point);
			var ang =  Math.abs(edgeVector.angle) > 90 ? 180 + edgeVector.angle : edgeVector.angle;
			return ang;
		};

		utils.toMeter = function (pixelDist) {
			return (pixelDist / 100).toFixed(2)+"m\n";
		};

		utils.getVectorMidPoint = function (line,paper) {
			var point1 = line.firstSegment.point;
			var point2 = line.lastSegment.point;
			return new paper.Point((point1.x+point2.x)/2,(point1.y+point2.y)/2);
		};

        utils.exportCanvas = function(networkName,type) {
            var fileName,url;
            if(type == "json") {
                fileName = "export_"+networkName.replace("/(\/|?|<|>|\\|:|*|||/|.","_")+".json";
                url = "data:text/json+xml;utf8," + encodeURIComponent(paper.project.exportJSON({asString:true,precision:16}));
            } else {
                fileName = "export_"+networkName.replace("/(\/|?|<|>|\\|:|*|||/|.","_")+".svg";
                url = "data:image/svg+xml;utf8," + encodeURIComponent(paper.project.exportSVG({asString:true,precision:16}));
            }

            var link = document.createElement("a");
            link.download = fileName;
            link.href = url;
            link.click();
        };

		utils.updateActualPosUI = function(state,callbacks) {
			if(state.getActualPosTile() != undefined && state.getBestPosTile() != undefined) {
				var actualPos = {};
				actualPos.xTile = Math.round(state.getActualPosTile().bounds.x / state.getTileLenPx());
				actualPos.yTile = Math.round(state.getActualPosTile().bounds.y / state.getTileLenPx());
				actualPos.pxDistX = Math.abs(state.getActualPosTile().position.x-state.getBestPosTile().position.x);
				actualPos.pxDistY = Math.abs(state.getActualPosTile().position.y-state.getBestPosTile().position.y);
				actualPos.pxDistDirect = Math.sqrt(Math.pow(actualPos.pxDistX,2)+Math.pow(actualPos.pxDistY,2));
				actualPos.tileDistDirect = actualPos.pxDistDirect / state.getTileLenPx();
				actualPos.cmDistDirect = actualPos.pxDistDirect / state.getScope().pixelPerCm;
				callbacks.onActualPosCallback(actualPos);
			}
		};

		return utils;
	});
})();