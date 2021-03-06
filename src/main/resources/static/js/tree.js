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
		while(parent.level!=0){
			parent=findParentNode(parent)
			parent=findNodeById(parent)
			node.level++
		}
		
	})
	let options = initOptions()
	let config={nodes:new vis.DataSet(nodes),edges:new vis.DataSet(edges)}
	let container = $('#nodes')[0]
	let network = new vis.Network(container, config,options);
	//network.redraw()
	network.on("click",  (params)=>{
        if(params.nodes.length===0)
        	return
        let nodeData=data.filter((d)=>d.id==params.nodes[0])[0]	
		//alert(JSON.stringify(nodeData))
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
		$('#downBtn').unbind('click')
		$('#downBtn').click(()=>{
			$.ajax({
		        url : 'certificate/download/'+nodeData.id,
		        type : 'get',
		        success(data) {
		            let blob = new Blob([data], { type: 'application/crt' })
		            let link = document.createElement('a')
		            link.href = window.URL.createObjectURL(blob)
		            let fileName=nodeData.commonName
		            fileName+='.crt'
		            link.download = fileName
		            document.body.appendChild(link)
		            link.click()
		            document.body.removeChild(link)
		        }
		        
		    });
			
		})
		if(!flag){
			$('#nodeR').append('<hr/><textarea class="form-control"  id="reason"></textarea><button type="button" id="revokeBtn" class="btn btn-primary">Revoke</button>')
			$('#revokeBtn').click(()=>{
				let id=$('#nodeID').html()
				$.ajax({
			        url : '/certificate/revoke/'+id,
			        type : 'post',
			        data:$('#reason').val(),
			        success : data=>{ 
			        	init()
			        	toastr.success('Certificate with ID: '+id+' revoked')
			        }
			       
			    });
			})
		} else{
			let id=$('#nodeID').html()
			$.ajax({
		        url : '/certificate/revokedReason/'+id,
		        type : 'get',
		        success : data=>{ 
		        	$('#nodeR').append(' ('+JSON.parse(data).reason+')')
		        }
		       
		    });
		}
		$('#nodeEnd').html(nodeData.endDate)
		$('#nodeStart').html(nodeData.startDate)
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
      height: (window.innerHeight - 75) + "px"	 
	}
	return ret
}

const createNode=data=> {
	let ret={}
	ret.id=data.id
	ret.label=data.commonName
	ret.issuer=data.issuerId
	ret.physics=false
	
	ret.shape= 'image'
	if(!isRevoked(data.id))
		if(!isPastToday(data.endDate))
			ret.image= '/img/certificate.png'
		else
			ret.image= '/img/expired.png'

	else
		ret.image= '/img/revoked.png'
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
	ret.physics=false
	ret.selectionWidth=0
	return ret
}


const init=()=>{
	$.ajax({
		url: '/certificate/getAll',
        type: 'get',
        success (nodes) {
        	displayNodes(nodes)
     	}
	});
	
	$('#logoutlink').click(function() {
		localStorage.setItem('jwtToken', null);
		window.location.href = '/index.html';
	});
	
	$('#create_cer').click(function() {
        $.ajax({
            url : '/certificate/create',
            type : 'get',
            success : function(data) {
                window.location.href = data;
            },
            error : function(data) {
                alert("nije uspeo");
            },
        });
    });
    
    $('#show_all').click(function() {
		window.location.href = "showAllCertificates.html"
	});

	$('#show_tree').click(function() {
		window.location.href = "tree.html"
	});
}


const getToken=()=> localStorage.getItem('jwtToken')

//salje token sa svakim zahtevom
$(document).ajaxSend((event, jqxhr, settings) =>{
	var token = getToken();
	if(settings.url.includes('https'))
		return;
	if (token != null)
		jqxhr.setRequestHeader('Authorization', 'Bearer ' + token)
});

//pokusava da produzi vreme trajanja tokena
function refreshToken(){
	if(getToken())
	$.ajax({
		   url: '/user/refresh',
           type: 'post',
           success: function (data) {
        	   localStorage.setItem('jwtToken',data.accessToken);
        }
	});
}

const isPastToday=(date)=>{
	let today=new Date()
	let x=date.split(' ')
	let d=new Date(x[1]+' '+x[2]+' '+x[5])
	return d<today
}

$(document).ready(init)