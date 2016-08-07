(function() {
	'use strict';

	angular.module('signalmap-ng').factory('signalmapInit',['signalmapState','signalmapUtils','signalmapUpdateAndTransform','signalmapCallbacks', function(state,utils,update,callbacks) {
		var initService = {};

        initService.redrawCanvas = function(paper,scope) {
            if(scope != undefined) {
                console.log("signalmap: draw canvas");

                var signalMap = scope.signalMap[scope.freq];

                state.constructor(paper, scope,
                        signalMap.tileLengthCm * ((scope.pixelPerCm > 1) ? scope.pixelPerCm : 1),
                    signalMap.lengthX, signalMap.lengthY, signalMap.floorplanConfig);

                initService.initSizeAndGrid(state.getLocalPaper());
                initService.initGraph(state.getLocalPaper(), signalMap, state.getGraphGroup(), state.getEdgeGroup(), false);
                initService.intiFloorplan(state.getLocalPaper(), state.getFloorplanRaster(), state.getFloorplanConfig());
				initService.initLoadingLayer(state.getLocalPaper(),state.getLoadingLayerGroup());

                update.setCorrectZOrdering();
                update.updateView();
	            callbacks.onCreate();
            } else {
				console.log("ignore init, not ready");
			}
        };

		initService.initLoadingLayer = function(paper,loadingGroup){
			if(loadingGroup && loadingGroup.removeChildren) { loadingGroup.removeChildren();}
			var fullScreenRect = new paper.Shape.Rectangle(new paper.Point(0,0),paper.view.viewSize);
			fullScreenRect.fillColor = 'black';
			fullScreenRect.opacity = 0.4;

			var text = new paper.PointText();
			text.content="Loading";
			text.justification='center';
			text.fillColor = 'white';
			text.position = paper.view.center;
			text.fontSize= 20;

			text.moveAbove(fullScreenRect);
			loadingGroup.addChild(fullScreenRect);
			loadingGroup.addChild(text);
			loadingGroup.visible = false;
		};

        initService.initSizeAndGrid = function (paper) {
            var usedGridLength = update.setViewSize();
            callbacks.viewSizeChanged(paper.view.viewSize);
            initService.initGridLines(paper,state.getGridGroup(),usedGridLength,usedGridLength, paper.view.bounds);
        };

		initService.initGraph = function(paper,signalMap,graphGroup,edgeGroup,useOriginalPosition) {
            console.log("signalmap: init graph");
			if(edgeGroup && edgeGroup.removeChildren) { edgeGroup.removeChildren();}
			if(graphGroup && graphGroup.removeChildren) { graphGroup.removeChildren();}

			if (signalMap) {
				var mac,node,edge, i;
				for(mac in signalMap.managedNodes) {
					node = signalMap.managedNodes[mac];
					if(useOriginalPosition) {
						node.currentPos = {x:node.originalPos.x,y:node.originalPos.y};
					}
					addSensor(paper,graphGroup,node.mac,node.name, node.currentPos.x*state.getTileLenPx(), node.currentPos.y*state.getTileLenPx(),true);
				}

				for(mac in signalMap.extendedNodes) {
					node = signalMap.extendedNodes[mac];
					if(useOriginalPosition) {
						node.currentPos = {x:node.originalPos.x,y:node.originalPos.y};
					}
					addSensor(paper,graphGroup,node.mac,node.name, node.currentPos.x*state.getTileLenPx(), node.currentPos.y*state.getTileLenPx(),false);
				}

				for (i = 0; i < signalMap.mangedNodeEdges.length;i++) {
					edge = signalMap.mangedNodeEdges[i];
					addEdge(edgeGroup,paper,edge.fromMac,edge.toMac);
				}

				for (i = 0; i < signalMap.extendedNodeEdges.length;i++) {
					edge = signalMap.extendedNodeEdges[i];
					addEdge(edgeGroup,paper,edge.fromMac,edge.toMac);
				}
			}
			graphGroup.moveAbove(edgeGroup);
			graphGroup.addChild(edgeGroup);
			update.updateView();
			return graphGroup;
		};

		function addSensor (paper,graphGroup,mac,name,x,y,isManagedNode) {
			var group = new paper.Group();
			var vertex = new paper.Shape.Circle(new paper.Point(x, y),state.getTileLenPx());
			if(isManagedNode) {
				vertex.fillColor = '#C0392B';
			} else {
				vertex.fillColor = '#446CB3';
			}

			group.onMouseDown = function() {
				if(!state.getDragModeGraph() && !state.getDragModeFloorplan()) {
					group.children['node_bounds'].opacity = 1;
				}
			};
			group.onMouseUp = function() {
				if(!state.getDragModeGraph() && !state.getDragModeFloorplan()) {
					group.children['node_bounds'].opacity = 0;
					update.updatePositionOfVertexGroup(mac,utils.getSnapPosition(group.position,state.getTileLenPx()));
					update.updateEdge(mac);
				}
			};
			group.onMouseDrag= function(event){
				if(!state.getDragModeGraph() && !state.getDragModeFloorplan()) {
					update.updatePositionOfVertexGroup(mac,utils.getPointWithMousePosOffset(event.delta, group.position,paper));

					update.syncPositionWithDataModel(mac);
					update.updateEdge(mac);
				}
			};

			var vertexText = new paper.PointText();
			vertexText.content=name+"\n\n\n"+mac;
			vertexText.justification='center';
			vertexText.position =vertex.position;
			vertexText.fontSize= 12;
			vertexText.style.shadowColor = 'white';
			vertexText.style.shadowOffset= new paper.Point(1,1);
			vertexText.style.shadowBlur= 1;
			vertexText.moveBelow(vertex);

			var boundsRec = new paper.Shape.Rectangle(vertexText.bounds);
			boundsRec.name = 'node_bounds';
			boundsRec.strokeColor = '#446CB3';
			boundsRec.dashArray = [4, 2];
			boundsRec.strokeWidth = 2;
			boundsRec.fillColor = new paper.Color(137, 196, 244,0.3);
			boundsRec.opacity = 0;

			var signalRadius = new paper.Path.Circle(vertex.position,state.getScope().signalMap[state.getScope().freq].defaultSignalRadiusCm);
			signalRadius.fillColor = {
				gradient: {
					stops: [['#3FC380', 0.2],['#F5D76E', 0.7], [new paper.Color(245, 215, 110,0), 1]],
					radial: true
				},
				origin: signalRadius.position,
				destination: signalRadius.bounds.rightCenter
			};
			signalRadius.name="signalradius";
			signalRadius.opacity=0.2;
			signalRadius.visible =false;

			group.addChild(boundsRec);
			group.addChild(vertex);
			group.addChild(vertexText);

			state.getVertices()[mac] = {};
			state.getVertices()[mac].path = vertex;
			state.getVertices()[mac].text = vertexText;
			state.getVertices()[mac].group = group;
			state.getVertices()[mac].signalStrengthCircle = signalRadius;
			state.getVertices()[mac].isManagedNode = isManagedNode;

			group.bringToFront();
			graphGroup.addChild(group);
			graphGroup.addChild(signalRadius)
		}

		function addEdge(edgeGroup,paper,macFrom,macTo) {
			if(state.getVertices()[macFrom] && state.getVertices()[macTo]) {
				var edge = new paper.Path.Line(state.getVertices()[macFrom].path.position, state.getVertices()[macTo].path.position);
				edge.strokeColor = '#3498DB';
				edge.strokeWidth = 1;
				edge.sendToBack();

				var edgeText = new paper.PointText(utils.getVectorMidPoint(edge,paper));
				edgeText.rotate(utils.getTextAngle(edge));
				edgeText.justification = 'center';
				edgeText.fontSize= 12;
				edgeText.content = utils.toMeter(edge.length);
				edgeText.moveAbove(edge);

				if(!state.getVertices()[macFrom].edges) {
					state.getVertices()[macFrom].edges = {};
				}
				if(!state.getVertices()[macTo].edges) {
					state.getVertices()[macTo].edges = {};
				}

				var edgeInfo = {'edge':edge,'text':edgeText,'angle':utils.getTextAngle(edge)};
				state.getVertices()[macFrom].edges[macTo] = edgeInfo;
				state.getVertices()[macTo].edges[macFrom] = edgeInfo;

				edgeGroup.addChild(edge);
				edgeGroup.addChild(edgeText);
			}
		}

		initService.intiFloorplan = function (paper,floorplanRaster,floorplanConfig) {
            console.log("signalmap: init floorplan");

            if(floorplanRaster) {floorplanRaster.remove()}

			if(document.getElementById('blueprint_image') != null) {
				floorplanRaster = new paper.Raster('blueprint_image');
			} else { //no blueprint set yet
				floorplanRaster = new paper.Raster();
				console.log("Image was not found in DOM for given id therefore it could not be loaded.");
			}

			if(floorplanConfig == undefined || (floorplanConfig.posX==-1 && floorplanConfig.posY==-1)) {
				console.log("signalmap: load defaults for floorplan");
                floorplanConfig = {};
				floorplanConfig.posX = paper.view.center.x;
				floorplanConfig.posY = paper.view.center.y;

				var longestCanvasSide = Math.max(state.getGridLengthX(),state.getGridLengthY())*state.getTileLenPx();
				var longestImgSide = Math.max(floorplanRaster.width,floorplanRaster.height);

				if(longestCanvasSide < longestImgSide) {
					floorplanConfig.scale = longestCanvasSide/longestImgSide;
				} else {
					floorplanConfig.scale = 1;
				}

                callbacks.floorplanScaleChanged(floorplanConfig.scale,state.getScope().freq); //TODO try remove scope from here
				update.floorplanScale(floorplanConfig.scale,floorplanRaster,floorplanConfig);
			}


			if(floorplanRaster) {
				floorplanRaster.position.x = floorplanConfig.posX;
				floorplanRaster.position.y = floorplanConfig.posY;
				floorplanRaster.scale(floorplanConfig.scale);
				floorplanRaster.sendToBack();

                callbacks.floorplanSizeChanged(floorplanRaster.size);
			}

			update.syncFloorplanAndUpdateView();
            state.setFloorplanRaster(floorplanRaster);

			return floorplanRaster;
		};

		initService.initGridLines = function (paper,gridGroup,num_rectangles_wide, num_rectangles_tall, boundingRect) {
            console.log("signalmap: init gridline");

            if(gridGroup && gridGroup.removeChildren) {gridGroup.removeChildren();}

			var width_per_rectangle = state.getTileLenPx();
			var height_per_rectangle = state.getTileLenPx();
			var i,line;
			for (i = 0; i <= num_rectangles_wide; i++) {
				var xPos = boundingRect.left + i * width_per_rectangle;
				var topPoint = new paper.Point(xPos, boundingRect.top);
				var bottomPoint = new paper.Point(xPos, state.getTileLenPx()*num_rectangles_tall);
				line = new paper.Path.Line(topPoint, bottomPoint);
				line.strokeColor = '#eaeaea';
				line.strokeWidth = 1;

				if(i== 0 || i == num_rectangles_wide) {
					line.strokeWidth = 3;
				}
				gridGroup.addChild(line);
			}
			for (i = 0; i <= num_rectangles_tall; i++) {
				var yPos = boundingRect.top + i * height_per_rectangle;
				var leftPoint = new paper.Point(boundingRect.left, yPos);
				var rightPoint = new paper.Point(state.getTileLenPx()*num_rectangles_wide, yPos);
				line = new paper.Path.Line(leftPoint, rightPoint);
				line.strokeColor = '#eaeaea';
				line.strokeWidth = 1;

				if(i== 0 || i == num_rectangles_tall) {
					line.strokeWidth = 3;
				}
				gridGroup.addChild(line);
			}
			return gridGroup;
		};

        initService.undoChangesGraph  = function (signalMap) {
            initService.initGraph(state.getLocalPaper(),signalMap,state.getGraphGroup(),state.getEdgeGroup(),true);
			update.setCorrectZOrdering();
			update.updateView();
		};

		return initService;
	}]);
})();