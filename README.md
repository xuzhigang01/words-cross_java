这是一个完整Crossed words building示例代码，实现了：交叉单词—生成及展示

代码核心是WordsCrossBuilder类，其他代码用于构建嵌入Jetty的WEB微服务。

可在IDE中直接运行Main.class，或按dist目录下的USAGE说明来启动服务。然后在浏览器中访问，如：
![web page](https://raw.githubusercontent.com/xuzhigang01/words-cross_java/master/h5.jpg)
> 可多点几次build按钮，查看不同的结果


##### 交叉单词矩阵生成步骤：
1. 根据单词总长度、最长单词长度、最短单词长度等，计算出初始矩阵尺寸，生成了一个空矩阵。乱序后的单词将逐一放置到矩阵里。
2. 每个单词，首字母放到矩阵的每个单元格，单词横放或竖置，测试该位置是否可用，可用则给出单词在此位置的评分。最后，选择评分最高的位置放置单词。
3. 测试该位置是否可用：
   * 单词放到这里不会超出矩阵；
   * 所占用的单元格没被同向单词使用；
   * 上下左右没有单词相接；
   * 若有交叉，交叉点字母相同。
4. 单词在此位置的评分：
   * 单词越长，评分越高；
   * 越靠近矩阵中心，评分越高；
   * 交叉点、特别是非头尾交叉点越多，评分越高；
   * 单词横向靠上下边、纵向考左右边，会减分。
5. 若有多个位置都是最高分，则随机选取一个位置放置单词。
6. 最后，若放不下所有单词，则重新打乱单词顺序再摆放。重试100次，还不能成功摆放所有单词，则扩展矩阵，长度或宽度+1。扩展后，重试100次，还不行，就再扩展再重试，成功为止。
7. 放置所有单词后，若矩阵周围有空边，则会裁掉空边。

##### 效果解析：
1. 交叉单词摆放，理论上有最优解。但在实现上，除穷举外没有太好的办法，穷举法计算量太大，不可取。因此，只能追求次优解。
2. 矩阵初始尺寸，很重要。从小尺寸开始摆放单词，能放置下所有单词的，必然是结构紧凑、交叉点多的组合，可认为是一个次优解。代码中重试100次再扩展尺寸，其实就是一个寻求次优解的过程。
3. 矩阵初始尺寸生成和扩展上，是长度优先。所以，矩阵一般是长方形。使纵向摆放的单词比横向的多，是因为：在视觉上，人眼对竖立的东西更敏感，如树、高楼等。
4. 基于上面描述的位置评分规则，使单词摆放倾向于：长单词靠近矩阵中心、非头尾交叉点较多，看起来重心居中、错落有致，给人稳重、协调的感觉，单词越多越明显。

