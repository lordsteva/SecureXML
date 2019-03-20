const displayNodes=(data)=>{
	let nodes=[]
	let edges=[]
	data.forEach((node)=>{
		nodes.push(createNode(node))
	})
	const findNodeById=nodeId=>data.filter((n)=>n.id===nodeId)[0]
	nodes.forEach((node)=>{
		let issuer=findNodeById(node.issuer)
		if(node.id===node.issuer)
			node.level=0
		else
			node.level=1
			//todo
		edges.push(createEdge(node,issuer))
	})
	
	 var options = {
		  layout:{
			    hierarchical:{
			    	direction:"DU",
			    	enabled:true
			    }
			  },
		  interaction: {
	          navigationButtons: true,
	          keyboard: true
	        },
	        height: '500px'
	}
	let config={nodes:new vis.DataSet(nodes),edges:new vis.DataSet(edges)}
	let container = $('#nodes')[0]
	let network = new vis.Network(container, config,options);
	network.on("click",  (params)=>{
        console.log('click event, getNodeAt returns: ' + network.getNodeAt(params.pointer.DOM))
        
	})
}

const createNode=data=> {
	let ret={}
	ret.id=data.id
	ret.label=data.commonName
	ret.issuer=data.issuerId
	return ret
}

const createEdge=(start,end)=> {
	let ret={}
	ret.from=start.id
	ret.to=end.id 
	ret.arrows='from'
	return ret
}


const init=()=>{
	$.ajax({
		url: '/certificate/getAll',
        type: 'get',
        success: function (nodes) {
        	displayNodes(nodes)
     	}
	});
	
}


$(document).ready(init)