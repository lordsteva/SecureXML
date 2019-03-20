const displayNodes=(data)=>{
	let nodes=[]
	let edges=[]
	data.forEach((node)=>{
		nodes.push(createNode(node))
	})
	const findNodeById=nodeId=>nodes.filter((n)=>n.id===nodeId)[0]
	nodes.forEach((node)=>{
		let issuer=findNodeById(node.issuer)
		edges.push(createEdge(issuer,node))
	})
	const findParentNode=(node)=>{
		let tempEdge=edges.filter((edge)=>
			edge.to===node.id);
		return tempEdge[0].from;
	}
	nodes.forEach((node)=>{
		if(node.level===0)
			return
		node.level=1;
		let parent=findParentNode(node)
		parent=findNodeById(parent)
		while(parent.level===undefined){
			parent=findParentNode(parent)
			parent=findNodeById(parent)
		}
		node.level+=parent.level
	})
	 var options = initOptions()
	let config={nodes:new vis.DataSet(nodes),edges:new vis.DataSet(edges)}
	let container = $('#nodes')[0]
	let network = new vis.Network(container, config,options);
	//network.redraw()
	network.on("click",  (params)=>{
        if(params.nodes.length===0)
        	return
        let nodeData=data.filter((d)=>d.id==params.nodes[0])[0]	
		alert(JSON.stringify(nodeData))
		$('#showNodeModal td').html('')
		$('#nodeID').html(nodeData.id)
		$('#nodeCN').html(nodeData.commonName)
		$('#nodeE').html(nodeData.email)
		$('#nodeL').html(nodeData.localityName)
		$('#nodeS').html(nodeData.state)
		$('#nodeO').html(nodeData.organization)
		$('#nodeOU').html(nodeData.organizationalUnitName)
		$('#nodeC').html(nodeData.country)
		let flag=isRevoked(nodeData.id)
		$('#nodeR').html(flag?'Yes':'No')
		if(!flag){
			$('#nodeR').append('<hr/><textarea class="form-control"  id="reason"></textarea><button type="button" id="revokeBtn" class="btn btn-primary">Revoke</button>')
			$('#revokeBtn').click(()=>{
				let id=$('#nodeID').html()
				$.ajax({
			        url : '/certificate/revoke/'+id,
			        type : 'post',
			        data:$('#reason').val(),
			        success : data=>{alert('toast dodaj')}
			        
			    });
			})
		}
		$('#nodeEnd').html(nodeData.startDate)
		$('#nodeStart').html(nodeData.endDate)
	//	let issuer=findNodeById() mrzi me dadodajem ovo xD
		$('#showNodeModal').trigger('click')
        //showModal()
	})
}

const initOptions=()=>{ 
	let ret={
		layout:{
		    hierarchical:{
		    	direction:"UD",
		    	enabled:true
		    }
		  },
	  interaction: {
        navigationButtons: true,
        keyboard: true
      },
      height: '500px'	 
	}
	return ret
}

const createNode=data=> {
	let ret={}
	ret.id=data.id
	ret.label=data.commonName
	ret.issuer=data.issuerId
	//ret.shape='icon'
   /* ret.icon={
        face: 'FontAwesome',
        code: '\uf1ad',
        size: 50,
        color: '#f0a30a'
      }
      */
	if(data.id===ret.issuer)
		ret.level=0
	
	return ret
}

const isRevoked=(id)=>{
	let ret
	$.ajax({
		url: '/certificate/isrevoked/'+id,
        type: 'get',
        async:false,
        success: (flag)=>{ret=flag}
	});
	return ret;
}

const createEdge=(start,end)=> {
	let ret={}
	ret.from=start.id
	ret.to=end.id 
	ret.arrows='to'
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