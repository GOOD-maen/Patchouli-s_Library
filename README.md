项目名称：Patchouli‘s Library

这是个人知识分享与记录平台，支持文章管理、社交互动、个性化设置。
简要描述：本项目是作者全栈独立开发的个人网站，前端采用 Vue 3 + Pinia + Tailwind，后端基于 Spring Boot 3.1 + JPA + MySQL，
          实现了文章发布、用户认证、评论点赞、图片裁剪、邮箱验证等功能，并集成了天气 API、WebSocket 实时通信等增强特性。

技术栈
前端：Vue 3（组合式 API）、Pinia、Vue Router、Vite、Tailwind CSS、Element Plus、Axios、cropperjs
后端：Java 17、Spring Boot 3.1、Spring Data JPA、MySQL、JWT、Spring Mail、Lombok
部署：本地运行（可扩展）

主要功能
用户注册/登录（JWT 认证，支持“记住我”）
文章发布、编辑、删除（支持封面图裁剪上传）
文章点赞、评论（含子评论/回复）
个人信息修改（头像上传、昵称、手机号）
后台管理（文章管理、用户管理）
天气显示（调用和风天气 API）

运行指南
环境要求：Node.js ≥ 20、Java 17、MySQL 8+

前端启动：

bash
cd frontend
npm install
npm run dev

后端启动：

bash
cd backend
mvn spring-boot:run

亮点与特色
前端路由数据预加载，提升用户体验
图片裁剪组件封装，适应不同场景（头像/封面）
全栈独立开发，完整实现前后端交互


欢迎提 Issue 或 PR，本项目仅供个人学习参考。

我的github网站:https://github.com/GOOD-maen/Patchouli-s_Library
