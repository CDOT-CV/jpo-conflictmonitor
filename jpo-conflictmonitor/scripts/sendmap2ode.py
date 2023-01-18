import socket
import time
import os

# Currently set to oim-dev environment's ODE
UDP_IP = os.getenv('DOCKER_HOST_IP')
UDP_PORT = 44920

# Supported MAP
rsu_13 = "0012835138023000205e9c094d3eab092ca624b5518202dc3258042800000400022f163328027d9b522afebe04e22f5a3070027d99bff3e9804f1339ec7fd009ec1fb71ff20b1380008012c03140000020001978ae901813ecbdbabeb809f6670e6fd5813b0beac3e7809ec675a6fec813c4bd30bf2409f666147021813ec3eccc0001625000100258082800000200012f14af84027d99e09c02804ec270ec28027d85a251ca0b0c40003011006220000000178db602813ec0db6071005220000000178db6b5013ec0d4a001007220000000178f6524813ec0db61b100c4200000000489012c080087100d420000000844c012c081c7a040100020081cb028d000000800051560840b1fff0a9409d863fae5b40141457fef53b76402246701414c836c10e02851b036cbf6a052302c6a0000804b024d00000080005562103b013ecc8024d64027d98ff60dfc04f631fde03bc09f66c02fa56b0143cc83545b6028a190be0b78051e02c6e0000804b02cd0000004000114af03db1fe0175809e21c270b900b1240001012c0834000000800095c2a0b804fb09e6c1718ffd863e04ec09e1c670060e40002581ea800000400012cd36c0602829ab70b0034050f354625ff680a322aec9400682c1a0001004b04150000008000259b7dae005053540ee003c0a326a9cf3fed8143c419de0010582c0002009608aa00000080004b36f3b900a0a6aea23ff70143cd51497ffe028c843c8e980b1a40007011012620000000167288ef814281378fc8404d88000000059cbe638050a04fd400101462000000016744a66814280072002c17740000020001148ddede050a360386c0000a1e6c01452d1013d8c8083f6302788100400d8025b3a2027d816190000c02582ce8000004000128cefde80a146c02e54758143cd8059ad3e027b1b00b5a54204ec02c360001804b055d0000002000450c7fc781428b80871b80a0a6402bbb3814284819b2ca6c02ad70d813c4c804f041027b00628400025830e80000020001297a7e180a14640398f301414b82c7b440a0a53b8fa5014140b0640001012c1974000001000054e57ef2050a3200ec7c80a0a5be9717c050502c150000404406a080000000556a7b70141487e76002828406e0800000005628fb00141487e74c028284072080000001571bfa90141407cb5a0401038024a52058c4000100806050502c920001004b02d90000008000459fa164404fb30a8580a00a14619c306701414c32ce10e02829659081f814141029030164b000080200"
rsu_14 = "0012829138023000205e9e094d3f377e2ca625de518202dc262022c400000002b7375bc09f603f3cf08083100000000abefda6027d810072e201ec400000002a92367c09f60401d616072a000000800022650d7724019dfd05c9f73058a20000809605aa00000040002a1e3d70027d9901ff2f604fb0a1c7bd0061440002581aa80000040001892eb5a590107ef204fb2e0dfbaf028298fdce51404f61602b3cfbc160b0000c025818a8000004000228dcf5b809f624037cfaadfa19fd027d87024d7bb2035da0409f66c0217630814140b068000601100a4200000001677d900013ec01ba062c08340000008000567766ab813ec429f602d1198c2a00c1c80004b024d000000c000c59da1e6804fb10ab8803441e6f28333c3b11436ed0e05050cff2c8e506f9f601414202218f19e6d196b3a4a97c0a0a100a276289319cc0a0a117e3b60894ddb056290000802b0f40004021005220000000049a03a88800aa027d8401888000000051b58e7013ec07e8bd10072200000001438038204fb01fa328b00450000002000156d00e9013ecc7ec8362028514ed61c4050500c5080004b00850000008000256030ea013ecc7f3c44a028518ffb8f2e050a36000405e809c40588400010096018a0000010000aaa771d0027d98ffd8ae805052df7623702851701218681414280c2e05c034dc4050535ff0043f809ba0587c00010096020a0000008000228e01d7640642c681414b8499dfc0a0a0585200028096093a00000040002bc08ca6409ec6683aff2013ec1cd0e7c0622400025826e80000060004abf9e4e809ec66a72feb813ec2da78a5667e0feb014144d12a0001737c750f23b29817105deabbf776c09f61d6fb004ab32f6e027d856150001002b054000802101482000000015fd8c3804fb23d780009f69ad3e027b1b00b5a54204ec02c360001804b055d0000002000450c7fc781428b80871b80a0a6402bbb3814284819b2ca6c02ad70d813c4c804f041027b00628400025830e80000020001297a7e180a14640398f301414b82c7b440a0a53b8fa5014140b0640001012c1974000001000054e57ef2050a3200ec7c80a0a5be9717c050502c150000404406a080000000556a7b70141487e76002828406e0800000005628fb00141487e74c028284072080000001571bfa90141407cb5a0401038024a52058c4000100806050502c920001004b02d90000008000459fa164404fb30a8580a00a14619c306701414c32ce10e02829659081f814141029030164b000080200"
rsu_18 = "0012829c38023000205ea4094d3885c02ca734da51a002dc225824280000020002af29f3bf027d97f048af013ecbdacc92409f66639f304013ec4c4428a81ed931b62dfdd500583a00040096088a0000018000abcdbd52409f65f36e25404fb2f97d294027d998fd4b6204fb33144a4100a0a66d6f912013eccc2a6e26027d856150001002b084000802100f72000000015acbd8004fb0290ea44041c80000000578274e813ec0a2bcb2c0124000002000103e68dcb18af30180a1462c6a2a581428457744291ae14824d67543db8027881621000040258044800000400020721a5062e944040143cc5e0c8fa028298aed086405052d6c639f027d9acf607cb204f102c3e0000804b00cd000000400025b5d4d360505298144d40a0a6b9c3af0401428c5afc6780282816148000a025808680000020001ad7d2683028294cc236605052d7c24e1028298bbc1244050509c38b8c16128000a022014a400000000cb51277083878e2012a400000000c94af3748bc9ee05051606b200000100002a56be25027d87338cb28ab5bec4160d0000c02581cc8000004000229c578ac09f61d0efa76b9a7efe409f61d66fd10ca34f514027d875e6e9d82c160001804b03190000008000911ed70db2918e37809ec1d4a3ad2ca68f70d027d995b2707404fb32ba5e3a409d85d20797804fb2a64f10c0a0a5cb5b96205053227ac42809f65c287b2c04fb02c1e0001804b02d90000008000a11226fc32941e51409ec251d5b69b2929d57409f65d8938fa04fb2ec5dc96027d9769af31013c4c99cb1ec027d9724ccd201414b903e6640a0a5c4df9be04fb2e143be2027d816110000c0240108800000002cb2693d028280c097e400e8800000002cdfc8db028280bd95a400c880000000281480e80a0a03306b1002a200000000b4ca1f780a0a03306500003261dcd9809f60419e5880bb900000000cc42dc97027b0195c0087c000400960e420000010000ebc12cc3009f666c7efc6813c4bccdbf0409f6167f3be2b39dc3c09f61e42d91abd21277409f62785e49e8f3d67c105884000400880fc900000000bc09b8b409f60612fd08104900000000bc09b22809f60685031410d200000040006bc93a49809f60aa727237e411e1701038981414c7f48cbc028280008bf08da05e36ab61e90dc4560e8000602b084000302101b82000000005d503960d958d0"
rsu_19 = "0012830538023000205ea2094d3a3e202ca7783451fa02dc385824e8000002000089d7fa3c8ff96d5837a9e7a4163680002025822e80000020000a97dfab40a0a6c00bd21d013ec3792fad4163880002025820e80000040001a92e3aa00a0a6bfe6538c813ecc7fd3303027d9b01b5ac0004e235fe3698cc09e2058240003009607ba00000100006a32eeb6028298ffa7e6a04fb31ffdfea809f66c0276fe601374c7d7f795027b0160b0000c0220270400000000a7ff8a4108e8220290400000000ae7b8bc106654582f6800000400008bb12d889d763ed6cd4d1f3d027b01617000100258316800000400010bada8407c8f7f7b37cb7c0c09f65ef01f8e04fb02c2a0002004b066d0000004000117754649317d7f1c5f5af8e0b0540003012c1ab4000001000045d8ef044d741f7c5e72e30c04fb02c110000c04406f08000000017372c9833182c4073080000000172ba0f033d800b07b90000002000117ba05905140476bfcd2edd813d8031d200012c03140000010000451d42d0c808c5e8027d97035116813ec2b0a4000501485a000296010a000001000062a421736408a2e5813ecd802b5daa027b1b00507f4404e736005d134009b00589c00010096008a000001000082bb615e6407e1fa013ecb806072409f66405040a013ecd806744ff02739affa48a8604d802c520000804b024d000000400015070bfa01414c2f6e02e0282866dd75682c4d0001c04b020d000000400005073b5901414c18be01d0282816288000e02580e680000040001283196680a0a610c6ff481414338842b0258dfd82c6e0001004b018d0000008000250602268141443771fb058f7a150050508c8e05c1639000080220148400000002836e6fc0a0a0229030805a100000000a104b5c0282806d400202b240000000161566915810bff2400a48000000008d5480c0f18ae4008480000000093b88680f4880203ba40000000134e16a301c4422581cc800000100012cf576f9028283793bfb201ded0c09f6241b7bb408c3480004218400201aa4000000030ce28e000a0a01b50008062900000000c33b621c0282805ebf40c1bb4000001000095e1b2040505137db7fe87fda7b28f40f01115842000180ac1d0000c084077080000000176eb240354818407b080000000176322b03bb85c000be01114184c00084000302101b82000000005d503960d958d0"
rsu_20 = "0012834538023000205ea0094d3c71c12ca6ce5d527c02dc3458244800000400028cf6f61f62355f9101464b5aae70c0a0a631b181a81428d6f27a48302829adde94a56050f35bd42952c09c4058340003009609920000010000a2128e4162ead9980143cc566b6af028798af5eb7c050f11349ee19adcef4cca050a35b9e29be009ce0582c000300960a120000008000222dfe5b6303588d0143cb5ab683c0a0a058f2000080960a920000008000424d3e7162ecf8af8143cc4c9f8d6028a16876f54814140b1d4000101101632000000014e83cc805050181d34405cc80000000546a74c01414061f5e101832000000015559dae05050181ca8b06910000008000557f359801414bccb40000a0a5f6bdea8050533d307b6c0a1e27ea6fe0afa9b073027d99945c26c04fb02c360002004b06d1000000800045e50a3e40505331437dac0a0a6d098bf540143cbee5bf780a0a67f5501a013eccc9ba13c027d8161900010025822280000010401ac5437b1027d933b3360141495f5f840a0a62fef8c581428b624e1cc0a142187100008c3c80008220400203aa400000000f926e6b066f00080f29000000003e45b0481ccc0c583ec800000100012f552960027d9500e77e04fb32ab0218809ce0d89092231e2000118e900011601b200000080002abf4179027d9958e8ee004e20ae20254561a8000a02b0c40005022c0264000002000115b2c2bc04fb32acf1f7809c4658f42a6813c4caeb868f02761b281ea9e004dd364e2d53cc09c4058b40001009600b200000100008af30177027d9967b06aa04e232a272bec09e2652ce518813b0d953152c1026e9b2a82a47e04e202c5e0000804401dc8000000051e38fe013ec89fcca027d84019c8000000052bb904013ec89a097027d84015c80000000538b109813ec0988b31004720000000151dc3ee04fb225c22c09f62c0a840000020000965b0781813d8d599c7fef02789ab155007204f1356711ffbc09ec058ec0002009604c200000100002b336362809ec6a2fe40170139cd452d7ff502788163d0000802201b2400000002c80d40a027d8067c0020192400000002c825284027d905ec0004fb08085100000000c1ad65ac02788033c00201f4400000003068d912009e2011f0008075100000000c1a1e2f00278804abf4b08401428c342a0ce02828258a0604a6be959aee0e6050502c920001004b02d90000008000459fa164404fb30a8580a00a14619c306701414c32ce10e02829659081f814141029030164b000080200"
rsu_21 = "001282b738023000205e98094d403fdc2ca62658516402dc2e20138400000000ce8d1d609cc80c581c280000020000861e7f26c011d5350142837acf830162a8000202581a28000002000105767e86bff7d54c01428c80d35a5027d96f5ff70813ec0b1640001012c0c14000002000084a6b86ed802ebadc0285190176fc404f636010389100a0a0582400030096058a0000010000423d0c376c00353eb01428d803ab46e02789b015db206050a02c160001804b02850000002000311126173600fe94dc0a146c01452e0813ecd8065b590027b1b002f6a4a050f00c2480004403c8800000000e3bfc0095b7b1010220000000058d37640803bf2c1354000002000055fcd2e804fb332ac7fbc09f667a5100b813ec0b0980008012c1454000001000055fc0fdc04fb332c980e409f65ed2e18604fb0ac110000c0560a8000604202ac400000002bde191809f606d703080b3100000000af7e4e8027d81ad40c58072800000200010a1b8ca470be29caa0b89c409f61c3285900b0940005012c02940000020000c55a25e4c8030e4d027d890061e14d7feb5f6102879afea0204404f602c3e0000804b0065000000800021637176b200719dc09f66bff0aaaf0143cd7fb11120027b016210000402200d4400000000875d06410e848200b44000000008ed50200fd0ba200944000000009590f980fd0f458116800000200028ce74ff421d51008ac6c2faa02829615575981414334fb97464816468c616b2f1587a000380ac410001c08b01ed000000a000119c35d0843bfa0225992211e050510c188000562d0000804b1580004032c18840000008000d78504e5813ec14e67c12c7cbc01414c7fbcc76027d9b002c80da050a00c5c80004b0491000000200025e16e9cc04fb07db1f05fa95f9804fb2f266fd8027d806224000240226800000003261dcd9809f60419e5880bb900000000cc42dc97027b0195c0087c000400960e420000010000ebc12cc3009f666c7efc6813c4bccdbf0409f6167f3be2b39dc3c09f61e42d91abd21277409f62785e49e8f3d67c105884000400880fc900000000bc09b8b409f60612fd08104900000000bc09b22809f60685031410d200000040006bc93a49809f60aa727237e411e1701038981414c7f48cbc028280008bf08da05e36ab61e90dc4560e8000602b084000302101b82000000005d503960d958d0"
rsu_22 = "0012839338023000205e96094d40df4c2ca626c8516e02dc3c2010640000000289e01c009f603f42e88039900000000a41107b027d80fd0a4200c6400000002973021c09f603de0c16029200000080002a8a008d027d98fee805404fb0e1085f60588200028096021200000080002aa0007d027d98fe9802e04fb1200c214456228000a02b1240005022c03240000020000d56b40bc04fb35ff655e2c09f623fb81c835fec0db240a0a2bff4aebf82c660000804b0089000000800025670034013ecd7fb9578e027d9aff883c4e050515ffa567a41635000040258024800000400012b8f81f409f663fac094013ecd7fc83ddb02829affa480bc04fb02c6e0000804b09c5000000200035ea98a9604f60da6c7c113d505c35ffe941d409f65c05034c050500c9880004409bc800000006d2bd3cec813c40cde062c1fd400000200008791ea3db3cf380a009f666f05005813d80ffe0a0588c00040092106a00000000bc75cac009f66db54c04a813d80a100801241ed40000000078ebae3b6da7a008809e2050904008811f100000000bc72389009f60eca8002049c400000002f1b2ca3027d93a71fa813ec204bc400000002f1b2b34027b0397608880cd10000000039b8e1a51036820505080d51000000003a7461ed1036760505080dd1000000003b2f62311006260505160bca00000080002b785e2a80a0a6c028de728145037f1f9e456488000202b2540001022c1894000001000057058c5b81414d806dbcd4028a18f4df23a050502c8d0000404b05a5000000800035b6471bc05053602431f380a2864087bdb0141458064ab0d6c00053fc013ec0b0680006012c15940000020000d6c06c6581414d807fb972028a1901d78dc050536020ec1800a0a6c039d639813d80b0780006012c1494000002000096ab8c6581414d8062be32028a1b01417e04050a360172d77009e2058440003009409c200000040006b3486a480a0a1cab7134c8117dcc02879b018fae2c050f3601ced54809e21012720000000067fbad0007e7e84045c80000000100661580958004041c8000000019f3658401cdfa2c0d64000002000144016c02c36ddfff0282984acc1ee05052c36f0ac02828669d82da8f821480a0a10f140002c8e0001004b03190000008000519fd190c43b2e0066108b08401428c342a0ce02828258a0604a6be959aee0e6050502c920001004b02d90000008000459fa164404fb30a8580a00a14619c306701414c32ce10e02829659081f814141029030164b000080200"
rsu_23 = "0012833a38023000205e94094d4197032ca628ed51a002dc32201184000000028aa49fc09f603e8598803e10000000023f127403e82608036100000000257327f03f4489602da00000080006280c1ce54258f4013ecb80b95b40a0a1c170d00296b2100587a00028096025a00000080006298d1c8542287b813ecb800184c0a0a1bf30ff628793d5058820002809601da00000100006ab601e5027d98fe60e22050531fd232a40a0a6bfa097a0013ecd7fa4499a02760162f000040258056800000400012b38076809f66bfca2a0081428d7f972f5f027d9afebe92d804ec02c620000804b006d0000008000157208e8813ecd7f3b044f02829afeec916a04ec02c660000804b030900000080005101b2d530b3280d00a0a583d2318050530cae87e40a14189ee23e20048bd211d907582c7e0001004b02c9000000800045015a4b813ecc2ec1fe902851662c87001414c1c0e2b602850853143f2424ae0e0059040002008808190000000020a7b54017cfa88079900000000208a9c30142fd1408a20000004000433af5be43793cd9203fce69b006758720505160aaa0000008000226fcdf36404788901414b80170440a0a05902000080960b2a000000800042891e0963fe98890141427d8f4e5bb573b6050502c7d0000404b05150000008000412c9f01b2034e0b00a0a5c0ffbde05051600769858b0161681e5805cb0ed058340003009609aa00000100004241fdfd6c034557f01428580dcdcb72c039d4f802c1e0001804b0495000000800031165702b2028e0b00a0a24075e3cb6020b03000a0a2c050de3e02c220001804405d8800000005597eb1813ec085f411018620000000159e1aa404fb020ccd84065880000000572dea9013ec08471f2c1d8400000200021783e8e1013ecaf9a7c7027d999ac3f4804f62f2fcf54027d9598ae1204fb0f15eccc5e8c13d004fb0f69e6f317ed8b30f1ba98c0587c000400960e420000010000ebc12cc3009f666c7efc6813c4bccdbf0409f6167f3be2b39dc3c09f61e42d91abd21277409f62785e49e8f3d67c105884000400880fc900000000bc09b8b409f60612fd08104900000000bc09b22809f60685031410d200000040006bc93a49809f60aa727237e411e1701038981414c7f48cbc028280008bf08da05e36ab61e90dc4560e8000602b084000302101b82000000005d503960d958d0"
rsu_24 = "0012835a38023000205e92094d422ff12ca62afb51b402dc3e201384000000015e02174740fd15c5815a800000100000cd46a5f4a28c8604fb00c2480004a040500000020003199ead5c1bb1d447028d32ae03bac1028286ff2cb3880ca90000000049b3b7e3040da0960d32000000400043c87d939580eab604fb36cc01ff8009f66d97bc015013ec0319200012820940000008000978cd4c6813ec142221c8fbb045ec7f906b902850b03790000004000119e7a158c2115ffa027d967a27ad014140b1640007012c0ce40000010000867867b9b07ef7f6009f6583e9ff40505156fb1ff7856308000e02b1740007022c0be4000002000046791710b00d47f7809f66aa403fe0814142b1f800040148f400020807f90000000033efcd7805f400201de400000000cf8520001d900160992000001000062498de96c02b539481414c8117c9b02829b0133828a050a3601cebfc00a0a0583400030096091200000100004232fe0a6408fed701414d80cee55802879b00e7645c050502c1e0001804b04490000008000210e87088e02fee06c082796f01450d805cb3860282816110000c02582868000002000089c2b7a86f36fc62dfe9dde02828163e8000202582a6800000200018a2037046f14fda2de3fef7028286faad4f31fe3d2440a0a058f2000080880b21000000002a5ad17040198080ba1000000002bc5cf5040da0880c21000000002d35cde0419a9160eba000000800022fc47af5fb49fbc04f60f0c920405832000180960e3a000000800022fc48d95fc7602204f60b8d62c856108000602b0740003022c1b74000002000085fad436bf9d400009ec1e90a02edd1d48027027d8561f0001002b0e8000802101e82000000005e76b948d3a06027d8407e080000000178fa08235480009f6000800000000043846780800d60007000000000464e63207e8d0000600000000048f663e0818ca2c05b4000001000084cdc528387c064c70230370e2ec6a90587200028096025a0000008000227d926c1c40c33c37e60060161e8000a0258074800000400018a9447f48fdd84d0d7fdd5ef502829aff26792c04f135feed49e809f6058b4000100960152000001000042bdf1fa23f5217b35fdec7fbc09ec6bfe926c6013ec0b1780002012c01a4000002000085a703ce57fae5e8d6bfbc13d2813c457fd24a52058c4000100806050502c920001004b02d90000008000459fa164404fb30a8580a00a14619c306701414c32ce10e02829659081f814141029030164b000080200"
rsu_25 = "0012834d38023000205e90094d4314212ca62e2351f002dc345847e800000100018f87275311fca10851112d6bfc3af64013ecc7f8c1d1027d80644400022045c4000000033a5b6b9009f6064d0016028a0000008000226591e1241704d2ae12c0ae027d8161e8000a02580828000002000189f8c68c9042139ac7ec8d00027d870030b92e0a6025027d816208000a0258062800000400018a8305b9afee690f404fb31fe0116809f66bfc1a8db013ecd7fbb503e027b0162f000040258042800000400008adf8571afdd8054604f135fe3530d409ec058c400010096008a000001000022ce11326bf7102ed013c4d7f8d4929027b0163300004022010440000000085c8c5410018a200e44000000008b88be410017e200c440000000091a8bcc1031945818a800000400008cec0e376174101401414415da119059040002009605aa0000010000433aeb38d85b3400c05050ce4901121c6d06702c860001004404188000000059e5e6c60505010e800403d88000000019e0242284220002828a045d0000002000959af2d24050506fdfa71c35786a38c6ef38919363d63a17fd2472c0c2e0e6a38531cfff34c2b12eb615effde160b4200000080006a78bdf00282890a1628a38aa6a34728beb59240cd1fc16408000202582b080000020001a98bf7f40a0a1c2b77ec38d578fc72a7f9b1242fc334164280002025829080000040004a93377d80a0a24305a258e2859461cb87d2a39a4f5689326e3663ab0736c77d0fb48f147e6d65a7393a813d8cc2935f3027d8160d0000c025827080000040004a8d777dc0a0a2433fa148e3f7e341cbcbbf4392be4487491ecf12738c69c946a620c3c51f9b598d1703404f10f709ec90583c00030096094200000100010a1d9dfd028289123732a38b2e34c9206655239526540934be38639ec63a894e2e52ecb03b2ff027d9b31a941f204f602c220001804405e48000000014fd7010223bf0406248000000016036fe01f4c7c406648000000016bbef281fac4cb0731000000800041e0b66584cffa0285f475fde04fb33f71800009f6167c3be0b5c5ae416210001002583b8800000400028f0571e86d09c3ff7813ecceb0e00b027d858d0fea2d0476b169636b0200800161f00010022041a400000000be456b81904002043a400000000be01114184c00084000302101b82000000005d503960d958d0"
rsu_26 = "0012830838023000205e8e094d43f4f72ca6ca9f51b402dc2e5808280000020001894a8f75696f3158141422da0906270a7d601414a3812100282856188000a02b0d40005022c0314000002000144e8c3e6d66d05fca02851644e09081414d65915e8502829850686d6050a3135131e00a0a22b7e6ff02c4e0000804b008500000080005142a892359c497f900a14622c46ae01428424a02d16133417101428c4a64c1c028288b699d6a0b1480002012c011400000280014530405ac33fc79c0282985ca8bc4050a357ed55b980a0a6223c5f801428c4ac0bee0282868e51258ac5600008046304000420104400000000cea4f7702787f08039100000000206c692023e5c8803110000000021ae5b202566e0802910000000022e54aa01ee6e16059a0000008000221aedd511646040475e3d0589a00038096051a0000008001220d9cbc117b60708f46fdc2537970956f0ac287ffc05497850b53f56171bdc84ef39962779c0b158a2000380ac550001c08b024d0000002000519f5a72e33eb5bdc4839996355ee8ac4d718d43795e5744ebf86c4b188000401184100010c1c8000c40310800000001161779008abc4403508000000011d77fd00cfb50b0495000000400020ff8e963b26685056f9a2c3afb63a8163080002025822a800000400028a6cb5e07743cbc8f7e3efc268cbd1d2f775ee6027b1b4083521804fb3670fa906c09e20582c00030096082a0000010000828b5e5a26085a8113173d955b34cd438204f11395cff2db342d441604f102c1a0001804b03d50000008000413c7fbc92d84c4e4975ae5accba7b2ba027b1b4a9b5f0804fb367ea6a30c09e20583c00030096072a0000004000637386218996f7006cd30ba90027d9970763a604fb33174d87009f61186100008c3480008404d8800000001670cbd82dbbdc40518800000001728c2b82dbbf0405588000000017b8b9f02c4bf0b05dd00000040001179e9b78b98cac439fd1a8ac150000c0560c8000604582ce800000500010bf351685d7e5282ebf16a5e99092a04fb12c32000200561b0001004115200101882000000005a9c2f80c5104000ec7c80a0a5be9717c050502c150000404406a080000000556a7b70141487e76002828406e0800000005628fb00141487e74c028284072080000001571bfa90141407cb5a0401038024a52058c4000100806050502c920001004b02d90000008000459fa164404fb30a8580a00a14619c306701414c32ce10e02829659081f814141029030164b000080200"
rsu_27 = "0012835f38023000205e8c094d44c7e12ca7872c516402dc34580c28000002000109654a0d8bac16e204fb111f20965688f148014280b0e40005012c0514000001000084d3645235c613a96be8343813ec44a384ec0587a00028096020a0000010000a27dd19a5a8cc35604fb117ed3a648b1c1ab0c5294de702879656005301414b21082840a0a048ac000116008a000000400042bec0615ae0493e04fb0d6324cd1a9f89f6031b200012c031400000200014525c248b51106c009f6230207520d7807721a50475ec427c80902879878012b6050a02c5a0000804b00850000008000415468612d44e19b027d88b08146ac3dfc2ff02851894f995805052c8b410402850162f000040220144400000000cdeccf9035e290804910000000020f647602ec5108041100000000220b40e03023a080391000000002375374030e4596069a00000080002332acb1887584d20221c991058aa00038096061a0000008000832f9c67087864cd033cd4af140a72c00505086d21d485f1c04e2b16400070158ba00038116059a0000014000832b1bfb887f0cd4222449f311c0c8f8ce0f1636107702281414430a20001184900012c6e000100c403908000000019cbe7d6035656100f420000000067baabb00c992cb0415000000200061189f78ae9e5e22028297382e038141439626fec72e7f838e2d79186441994f01428c873fa5b02850461e4000230e200022c1154000002000184925cb8c998f3aa02850727fc860e641db51c707274c8ae72d0028297154f2501414c85db6cd0285016130000c025824a800000400028976b7099342e7c6050a0e4ff93a2483b931b2309cb840a0a643bd96e01414c857f6c10285016110000c025828a800000200018a2c3275747aeac81414b92e62500a0a1cc277b228e0eec058da0000809609aa0000010000c2739d385cf1f60a05052e825de802828923d652ec8f6378a02828514f996c86672d6028519087e3b2050502c1e000180440558800000000e9ce7e0b133b1016620000000058d14d60a1380101762000000005b853a009e74d2c18740000008000478b97318bce14a03c258210c5c8000462c40004315200032c197400000200004783b6660bbb556c3d21798ac3e000200561d00010045834e800000200008bf08da05e36ab61e90dc4560e8000602b084000302101b82000000005d503960d958d0"
rsu_28 = "0012834538023000205e8a094d454e092ca7a25a51be02dc2e58022800000100008b1085a58f8d1a2604f631ec125dc09ec118e900008c7880008b014500000040002135d88a2dee0392027b06f723882df9843e027d8161e8000a02580828000004000409ffc47dafcb8ba4204ec31ee0217c09f663b806c0013ecb6b2069409f61b6385da368d0ec56b9f04e813ecc6850e2d027d98b9d960804fb02c5a0000804b00c50000008000714b089935f8714e5c09d863d92678813d8370588318e5a9a7a04fb11bae2f4d6b9f00c813ecc6220ab7027d98c959d1604fb02c5e0000804b008500000080009155e8a331f3133ac09ec63de20ed813d8b77306d809ec1b8b83bcc7218d14027d86d63145ad888162027d96bc00e2013ec46788fa65a5cc04a04fb02c62000080440208800000005121855813ec07e8ed10072200000001475c15604fb02003704018880000000529204f813ec0800e12c0c540000020000c670a6d3355c95fa0009ec21b5afafb56762000009f66ab25401f813d80b1d80004012c0b540000020000c66b4632301497a2c09f66108ef65013ecd5f548061027d9a981f004404f602c7a0001004403d880000000101a4d2008a7e8b046100000020002501d758014141acb8f5906ae126050a321cff2200a1e0188100008808390000000040726234007ef4960aca00000080002a766dd602829908a729e050f29fbf7ec0a0a158ea000080ac790000408b05250000008000452f7eda01414c846b943028797096eda81414d819caa8c02879b02b36b36050f36024ac94409ce058340003009609ca00000100006a48dda4028299084f1b8050f360902e2180a286c0b15b0c8143cd80beb22602738120f0000c582528000004000228c8b5d80a0a64165a1401428d8154a10e0287990a477fc050a3605aadfd80a1e6c03a58798139c0b0880006011016a20000000151d1bcc0505020cbf0405e880000000553aef30141488332a02828406288000000055f06fb814148846fc02828b06ad00000080003576fd27014144e0260172730af59b35b37b8409f6670cb017013ec0b0f80008012c1bb4000001000095e1b2040505137db7fe87fda7b28f40f01115842000180ac1d0000c084077080000000176eb240354818407b080000000176322b03bb85c000be01114184c00084000302101b82000000005d503960d958d0"

print("UDP target IP:", UDP_IP)
print("UDP target port:", UDP_PORT)
#print("message:", MESSAGE)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) # UDP

while True:
  print("sending MAP every 1 second")
  sock.sendto(bytes.fromhex(rsu_13), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_14), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_18), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_19), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_20), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_21), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_22), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_23), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_24), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_25), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_26), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_27), (UDP_IP, UDP_PORT))
  sock.sendto(bytes.fromhex(rsu_28), (UDP_IP, UDP_PORT))
  time.sleep(20)